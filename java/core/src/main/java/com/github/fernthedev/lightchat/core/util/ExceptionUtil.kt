package com.github.fernthedev.lightchat.core.util;

import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.exceptions.DebugException;
import com.github.fernthedev.lightchat.core.exceptions.ParsePacketException;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.google.gson.GsonBuilder;

public class ExceptionUtil {

    public static RuntimeException throwParsePacketException(Exception e, Packet packet) {
        RuntimeException parsePacketException = new ParsePacketException("Unable to parse packet " + packet.getPacketName(), e);

        if (StaticHandler.isDebug()) parsePacketException = new DebugException("Unable to parse packet data: " + new GsonBuilder().setPrettyPrinting().create().toJson(packet), e);

        return parsePacketException;
    }

    public static RuntimeException throwParsePacketException(Exception e, String packet) {
        RuntimeException parsePacketException = new ParsePacketException("Unable to parse packet " + packet, e);

        if (StaticHandler.isDebug()) parsePacketException = new DebugException("Unable to parse packet data: " + new GsonBuilder().setPrettyPrinting().create().toJson(packet), e);

        return parsePacketException;
    }

}
