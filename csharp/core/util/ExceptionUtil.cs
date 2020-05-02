using System;
using com.github.fernthedev.lightchat.core.exceptions;
using com.github.fernthedev.lightchat.core.packets;

namespace com.github.fernthedev.lightchat.core.util
{
    public class ExceptionUtil
    {

        

        public static SystemException throwParsePacketException(Exception e, Packet packet) {

            SystemException parsePacketException = new ParsePacketException("Unable to parse packet " + packet._PacketName, e);

            if (StaticHandler.Debug) parsePacketException = new DebugException("Unable to parse packet data: " + StaticHandler.defaultJsonHandler.toJson(packet), e);

            return parsePacketException;
        }

        public static SystemException throwParsePacketException(Exception e, String packet) {
            SystemException parsePacketException = new ParsePacketException("Unable to parse packet " + packet, e);

            if (StaticHandler.Debug) parsePacketException = new DebugException("Unable to parse packet data: " + StaticHandler.defaultJsonHandler.toJson(packet), e);

            return parsePacketException;
        }
    }
}