using com.github.fernthedev.lightchat.core.encryption;
using com.github.fernthedev.lightchat.core.packets;
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Text;
using System.Text.Unicode;

namespace com.github.fernthedev.lightchat.core.codecs
{
    abstract public class Encoder<T>
    {
        abstract public void Encode(T msg, List<object> outList);
    }

    abstract public class Decoder<T>
    {
        abstract public void Decode(T msg, List<object> outList);
    }

    public class StringEncoder : Encoder<string>
    {

        private Encoding encoding;

        public StringEncoder(Encoding? encoding)
        {
            if (encoding == null) this.encoding = Encoding.UTF8;
            else this.encoding = encoding;
        }

        public override void Encode(string msg, List<object> outList)
        {
            outList?.Add(encoding.GetBytes(msg));
        }
    }
    public class LineEndStringEncoder : StringEncoder
    {

        public readonly string lineEndString;

        public LineEndStringEncoder(Encoding? encoding, string? lineEndString) : base(encoding)
        {
            if (lineEndString == null) this.lineEndString = "\n\r";
            else this.lineEndString = lineEndString;
        }

        public override void Encode(string msg, List<object> outList)
        {
            base.Encode(msg + lineEndString, outList);
        }
    }

    public class StringDecoder : Decoder<byte[]>
    {

        private Encoding encoding;

        public override void Decode(byte[] msg, List<object> outList)
        {
            outList?.Add(encoding.GetString(msg));
        }
    }

    public class EncryptedJSONEncoder : Encoder<IAcceptablePacketTypes>
    {

        private LineEndStringEncoder stringEncoder;
        private JsonHandler jsonHandler;
        

        public override void Encode(IAcceptablePacketTypes msg, List<object> outList)
        {
            PacketWrapper<Packet> packetWrapper;

            if (msg is UnencryptedPacketWrapper)
            {
                packetWrapper = msg as UnencryptedPacketWrapper;
            } else
            {
                Packet packet = (Packet) msg;

                // Encrypting the data
                String decryptedJSON = jsonHandler.toJson(msg);
                EncryptedBytes encryptedBytes = encrypt(ctx, decryptedJSON);

                // Adds the encrypted json in the packet wrapper
                packetWrapper = new EncryptedPacketWrapper(encryptedBytes, packet, encryptionKeyHolder.getPacketId(packet.getClass(), ctx, ctx.channel()).getKey());
                String jsonPacketWrapper = jsonHandler.toJson(packetWrapper);

                // Encodes the string for sending
                encoder.encode(ctx, jsonPacketWrapper, out);
            }
        }
    }

}
