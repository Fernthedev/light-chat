package com.github.fernthedev.lightchat.client.api

import com.github.fernthedev.lightchat.core.packets.Packet

fun interface IPacketHandler {
    fun handlePacket(packet: Packet, packetId: Int)
}