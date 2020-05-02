using System;
using System.Runtime.Serialization;

namespace com.github.fernthedev.lightchat.core.exceptions
{
    public class DebugException : SystemException
    {
        public DebugException()
        {
        }

        protected DebugException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }

        public DebugException(string? message) : base(message)
        {
        }

        public DebugException(string? message, Exception? innerException) : base(message, innerException)
        {
        }
    }
}