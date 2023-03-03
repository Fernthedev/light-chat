package com.github.fernthedev.lightchat.server.api

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.server.ClientConnection

fun interface IPacketHandler {
    suspend fun handlePacket(acceptablePacketTypes: AcceptablePacketTypes, clientConnection: ClientConnection, packetId: Int)
}