using System;
using System.Runtime.Serialization;

namespace com.github.fernthedev.lightchat.core.exceptions
{
    public class ParsePacketException : SystemException
    {
        public ParsePacketException()
        {
        }

        protected ParsePacketException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }

        public ParsePacketException(string? message) : base(message)
        {
        }

        public ParsePacketException(string? message, Exception? innerException) : base(message, innerException)
        {
        }
    }
}