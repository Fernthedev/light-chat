package com.github.fernthedev.lightchat.server.api

import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.server.ClientConnection

fun interface IPacketHandler {
    fun handlePacket(packet: Packet, clientConnection: ClientConnection, packetId: Int)
}