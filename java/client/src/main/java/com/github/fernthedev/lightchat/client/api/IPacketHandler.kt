package com.github.fernthedev.lightchat.client.api

import com.github.fernthedev.lightchat.core.packets.PacketJSON

fun interface IPacketHandler {
    fun handlePacket(packetJSON: PacketJSON, packetId: Int)
}