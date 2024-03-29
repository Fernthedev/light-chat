package com.github.fernthedev.lightchat.core.util

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.exceptions.DebugException
import com.github.fernthedev.lightchat.core.exceptions.ParsePacketException
import com.google.gson.GsonBuilder

object ExceptionUtil {
    fun throwParsePacketException(e: Exception, packetJSON: AcceptablePacketTypes): RuntimeException {
        var parsePacketException: RuntimeException =
            ParsePacketException("Unable to parse packet " + packetJSON.packetName, e)
        if (StaticHandler.isDebug()) parsePacketException = DebugException(
            "Unable to parse packet data: " + GsonBuilder().setPrettyPrinting().create().toJson(packetJSON),
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