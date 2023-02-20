package com.github.fernthedev.lightchat.server.api

import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.server.ClientConnection

fun interface IPacketHandler {
    fun handlePacket(packetJSON: PacketJSON, clientConnection: ClientConnection, packetId: Int)
}