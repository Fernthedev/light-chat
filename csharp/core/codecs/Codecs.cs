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
        //     current system character set.
        public LineEndStringEncoder() : base()
        {

        }

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
            base.Encode(context, message + StaticHandler.endLine, output);
        }

        public void EncodePublic(IChannelHandlerContext context, string message, List<object> output)
        {
            this.Encode(context, message, output);
        }
    }

    public class EncryptedJSONEncoder : MessageToMessageEncoder<IAcceptablePacketTypes>
    {

        private readonly IJsonHandler jsonHandler;
        private readonly LineEndStringEncoder encoder;

        protected IEncryptionKeyHolder encryptionKeyHolder;
        protected Encoding charset;


        /**
         * Creates a new instance with the current system character set.
         */
        public EncryptedJSONEncoder(IEncryptionKeyHolder encryptionKeyHolder, IJsonHandler jsonHandler) : this(StaticHandler.encoding, encryptionKeyHolder, jsonHandler)
        {
        }

        /**
         * Creates a new instance with the specified character set.
         *
         * @param charset
         */
        public EncryptedJSONEncoder(Encoding charset, IEncryptionKeyHolder encryptionKeyHolder, IJsonHandler jsonHandler)
        {
            encoder = new LineEndStringEncoder(charset);
            this.charset = charset;
            this.encryptionKeyHolder = encryptionKeyHolder;
            this.jsonHandler = jsonHandler;
            StaticHandler.core.logger.Debug("Using charset {} for encrypting", charset.EncodingName);
        }

        protected override void Encode(IChannelHandlerContext ctx, IAcceptablePacketTypes msg, List<object> output)
        {

            if (msg is UnencryptedPacketWrapper)
            {
                var packetWrapper = msg as UnencryptedPacketWrapper;

                encoder.EncodePublic(ctx, jsonHandler.toJson(packetWrapper), output);
            }
            else
            {
                Packet packet = (Packet)msg;

                // Encrypting the data
                string decryptedJSON = jsonHandler.toJson(msg);
                EncryptedBytes encryptedBytes = encrypt(ctx, decryptedJSON);

                // Adds the encrypted json in the packet wrapper
                var packetWrapper = new EncryptedPacketWrapper(encryptedBytes, packet, encryptionKeyHolder.getPacketId(new GenericType<Packet>(packet), ctx, ctx.Channel).Item1);
                string jsonPacketWrapper = jsonHandler.toJson(packetWrapper);

                // Encodes the string for sending
                encoder.EncodePublic(ctx, jsonPacketWrapper, output);
            }

        }

        private EncryptedBytes encrypt(IChannelHandlerContext ctx, string decryptedJSON)
        {

            if (decryptedJSON == null) return null;

            if (decryptedJSON.Length == 0)
            {
                return null;
            }


            RijndaelManaged secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.Channel);
            EncryptedBytes encryptedJSON = EncryptionUtil.encrypt(secretKey.Key, secretKey.IV, decryptedJSON, charset);


            return encryptedJSON;
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
            List<object> tempDecodeList = new List<object>();
            base.Decode(ctx, msg, tempDecodeList);

            string decodedStr = (string)tempDecodeList[0];
            //        StaticHandler.getCore().getLogger().debug("Decoding the string {}", decodedStr);
            PacketWrapper<object> packetWrapper = jsonHandler.fromJson<PacketWrapper<object>>(decodedStr);

            string decryptedJSON;

            try
            {
                if (packetWrapper.isEncrypted)
                {
                    var encryptedPacketWrapper = jsonHandler.fromJson<EncryptedPacketWrapper>(decodedStr);

                    EncryptedBytes encryptedBytes = jsonHandler.fromJson<EncryptedBytes>(encryptedPacketWrapper.getJsonObject);
                    decryptedJSON = decrypt(ctx, (encryptedBytes));
                }
                else
                {
                    var unencryptedPacketWrapper = jsonHandler.fromJson<UnencryptedPacketWrapper>(decodedStr);
                    decryptedJSON = packetWrapper.getJsonObject;
                }
            }
            catch (Exception e)
            {
                throw new ArgumentException("Unable to parse string: " + decodedStr, e);
            }

            output.Add(getParsedObject(packetWrapper.packetIdentifier, decryptedJSON, packetWrapper.getPacketId));
        }

        /**
         * Converts the JSON Object into it's former instance by providing the class name
         *
         * @param jsonObject
         * @return
         */
        public Tuple<Packet, int> getParsedObject(string packetIdentifier, string jsonObject, int packetId)
        {
            GenericType<Packet> aClass = PacketRegistry.getPacketClassFromRegistry(packetIdentifier);

            try
            {
                return new Tuple<Packet, int>(jsonHandler.fromJson(jsonObject, aClass), packetId);
            }
            catch (Exception e)
            {
                throw new ArgumentException("Attempting to parse packet " + packetIdentifier + " (" + aClass.Typee.Name + ") with string\n" + jsonObject, e);
            }
        }

        protected string decrypt(IChannelHandlerContext ctx, EncryptedBytes encryptedString)
        {

            if (!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.Channel)) throw new ArgumentException("No secret key available");

            RijndaelManaged secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.Channel);

            if (secretKey == null)
            {
                throw new ArgumentException("Secret key is null");
            }


            string decryptedJSON = null;



            decryptedJSON = EncryptionUtil.decrypt(encryptedString, secretKey.Key, secretKey.IV, charset);



            return decryptedJSON;
        }
    }

}
