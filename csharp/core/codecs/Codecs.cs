using com.github.fernthedev.lightchat.core.encryption;
using com.github.fernthedev.lightchat.core.packets;
using com.github.fernthedev.lightchat.core.util;
using DotNetty.Buffers;
using DotNetty.Codecs;
using DotNetty.Common.Utilities;
using DotNetty.Transport.Channels;
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Security.Cryptography;
using System.Text;
using System.Text.Unicode;

namespace com.github.fernthedev.lightchat.core.codecs
{


    public class LineEndStringEncoder : StringEncoder
    {

        //
        // Summary:
        //     Initializes a new instance of the DotNetty.Codecs.StringEncoder class with the
        //     specified character set..
        //
        // Parameters:
        //   encoding:
        //     Encoding.
        public LineEndStringEncoder(Encoding encoding) : base(encoding)
        {

        }

        protected override void Encode(IChannelHandlerContext context, string message, List<object> output)
        {
            base.Encode(context, message + StaticHandler.EndLine, output);
        }

        public void EncodePublic(IChannelHandlerContext context, string message, List<object> output)
        {
            Encode(context, message, output);
        }
    }

    public class EncryptedJsonEncoder : MessageToMessageEncoder<IAcceptablePacketTypes>
    {

        private readonly IJsonHandler jsonHandler;
        private readonly LineEndStringEncoder encoder;

        protected IEncryptionKeyHolder encryptionKeyHolder;
        protected Encoding charset;


        /**
         * Creates a new instance with the current system character set.
         */
        public EncryptedJsonEncoder(IEncryptionKeyHolder encryptionKeyHolder, IJsonHandler jsonHandler) : this(StaticHandler.encoding, encryptionKeyHolder, jsonHandler)
        {
        }

        /**
         * Creates a new instance with the specified character set.
         *
         * @param charset
         */
        public EncryptedJsonEncoder(Encoding charset, IEncryptionKeyHolder encryptionKeyHolder, IJsonHandler jsonHandler)
        {
            encoder = new LineEndStringEncoder(charset);
            this.charset = charset;
            this.encryptionKeyHolder = encryptionKeyHolder;
            this.jsonHandler = jsonHandler;
            StaticHandler.Core.Logger.Debug("Using charset {0} for encrypting", charset.EncodingName);
        }

        protected override void Encode(IChannelHandlerContext ctx, IAcceptablePacketTypes msg, List<object> output)
        {
            string jsonPacketWrapper = null;
            if (msg is UnencryptedPacketWrapper)
            {
                var packetWrapper = msg as UnencryptedPacketWrapper;

                encoder.EncodePublic(ctx, jsonHandler.toJson(packetWrapper), output);
            }
            else
            {
                var packet = (Packet)msg;

                // Encrypting the data
                var decryptedJson = jsonHandler.toJson(msg);
                var encryptedBytes = encrypt(ctx, decryptedJson);

                // Adds the encrypted json in the packet wrapper
                var packetWrapper = new EncryptedPacketWrapper(encryptedBytes, packet, encryptionKeyHolder.getPacketId( packet.GetType(), ctx, ctx.Channel).Item1);
                jsonPacketWrapper = jsonHandler.toJson(packetWrapper);

                // Encodes the string for sending
                encoder.EncodePublic(ctx, jsonPacketWrapper, output);
            }

            StaticHandler.Core.Logger.Debug("Sending {0}", jsonPacketWrapper);
        }

        private EncryptedBytes encrypt(IChannelHandlerContext ctx, string decryptedJSON)
        {

            if (decryptedJSON == null) return null;

            if (decryptedJSON.Length == 0)
            {
                return null;
            }


            var secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.Channel);
            var encryptedJson = EncryptionUtil.encrypt(secretKey, decryptedJSON, charset);


            return encryptedJson;
        }
    }

    public class EncryptedJSONDecoder : StringDecoder
    {

        private readonly IJsonHandler jsonHandler;

        protected IEncryptionKeyHolder encryptionKeyHolder;
        protected Encoding charset;

        /**
         * Creates a new instance with the current system character set.
         */
        public EncryptedJSONDecoder(IEncryptionKeyHolder encryptionKeyHolder, IJsonHandler jsonHandler) : this(StaticHandler.encoding, encryptionKeyHolder, jsonHandler)
        {
            StaticHandler.Core.Logger.Debug("Using charset {0} for decrypting", charset.EncodingName);
        }

        /**
         * Creates a new instance with the specified character set.
         *
         * @param charset
         */
        public EncryptedJSONDecoder(Encoding charset, IEncryptionKeyHolder encryptionKeyHolder, IJsonHandler jsonHandler) : base(charset)
        {
            this.charset = charset;
            this.encryptionKeyHolder = encryptionKeyHolder;
            this.jsonHandler = jsonHandler;
        }

        /**
         * Returns a string list
         *
         * @param ctx
         * @param msg The data received
         * @param out The returned objects
         * @throws Exception
         */
        protected override void Decode(IChannelHandlerContext ctx, IByteBuffer msg, List<object> output)
        {
            var tempDecodeList = new List<object>();
            base.Decode(ctx, msg, tempDecodeList);

            var decodedStr = (string)tempDecodeList[0];
            StaticHandler.Core.Logger.Debug("Decoding the string {0}", decodedStr);
            var packetWrapper = jsonHandler.fromJson<GenericJsonPacketWrapper>(decodedStr, typeof(GenericJsonPacketWrapper));

            string decryptedJson;

            try
            {
                if (packetWrapper.ENCRYPT)
                {
                    var encryptedPacketWrapper = jsonHandler.fromJson<EncryptedPacketWrapper>(decodedStr);

                    var encryptedBytes = jsonHandler.fromJson<EncryptedBytes>(encryptedPacketWrapper.jsonObject);
                    decryptedJson = decrypt(ctx, (encryptedBytes));
                }
                else
                {
                    var unencryptedPacketWrapper = jsonHandler.fromJson<UnencryptedPacketWrapper>(decodedStr);
                    decryptedJson = unencryptedPacketWrapper.jsonObject;
                }
            }
            catch (Exception e)
            {
                throw new ArgumentException("Unable to parse string: " + decodedStr, e);
            }

            output.Add(getParsedObject(packetWrapper.packetIdentifier, decryptedJson, packetWrapper.packetId));
        }

        /**
         * Converts the JSON Object into it's former instance by providing the class name
         *
         * @param jsonObject
         * @return
         */
        public Tuple<Packet, int> getParsedObject(string packetIdentifier, string jsonObject, int packetId)
        {
            var aClass = PacketRegistry.getPacketClassFromRegistry(packetIdentifier);

            try
            {
                return new Tuple<Packet, int>(
                    jsonHandler.fromJson<Packet>(
                        jsonObject, aClass),
                    packetId);
            }
            catch (Exception e)
            {
                throw new ArgumentException(
                    "Attempting to parse packet " + packetIdentifier + " (" + aClass.Name + ") with string\n" +
                    jsonObject, e);
            }
        }

        protected string decrypt(IChannelHandlerContext ctx, EncryptedBytes encryptedString)
        {

            if (!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.Channel)) throw new ArgumentException("No secret key available");

            var secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.Channel);

            if (secretKey == null)
            {
                throw new ArgumentException("Secret key is null");
            }


            var decryptedJson = EncryptionUtil.decrypt(encryptedString, secretKey, charset);



            return decryptedJson;
        }
    }

}
