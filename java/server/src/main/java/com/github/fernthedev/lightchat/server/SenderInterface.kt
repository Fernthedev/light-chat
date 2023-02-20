package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import io.netty.channel.ChannelFuture

interface SenderInterface {
    @Deprecated(
        "Use packet transport", ReplaceWith(
            "sendPacket(packet.transport(true))",
            "com.github.fernthedev.lightchat.core.encryption.transport"
        )
    )
    fun sendPacket(packetJSON: PacketJSON): ChannelFuture
    fun sendPacket(packet: PacketTransporter): ChannelFuture

    val name: String
}