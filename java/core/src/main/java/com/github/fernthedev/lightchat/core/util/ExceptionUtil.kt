package com.github.fernthedev.lightchat.core.util

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.exceptions.DebugException
import com.github.fernthedev.lightchat.core.exceptions.ParsePacketException
import com.github.fernthedev.lightchat.core.packets.Packet
import com.google.gson.GsonBuilder

object ExceptionUtil {
    fun throwParsePacketException(e: Exception, packet: Packet): RuntimeException {
        var parsePacketException: RuntimeException =
            ParsePacketException("Unable to parse packet " + packet.packetName, e)
        if (StaticHandler.isDebug()) parsePacketException = DebugException(
            "Unable to parse packet data: " + GsonBuilder().setPrettyPrinting().create().toJson(packet),
            e
        )
        return parsePacketException
    }

    fun throwParsePacketException(e: Exception, packet: String): RuntimeException {
        var parsePacketException: RuntimeException = ParsePacketException("Unable to parse packet $packet", e)
        if (StaticHandler.isDebug()) parsePacketException = DebugException(
            "Unable to parse packet data: " + GsonBuilder().setPrettyPrinting().create().toJson(packet),
            e
        )
        return parsePacketException
    }
}