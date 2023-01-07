package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.packets.Packet
import io.netty.channel.ChannelFuture
import lombok.AllArgsConstructor
import java.io.Serializable

@AllArgsConstructor
class Console(
    @Transient
    val server: Server
) : SenderInterface, Serializable {

    @Deprecated("", ReplaceWith("null"))
    override fun sendPacket(packet: Packet): ChannelFuture {
        return null!!
    }

    override fun sendPacket(packet: PacketTransporter): ChannelFuture {
        return null!!
    }

    override val name: String
        get() = server.name

    companion object {
        private const val serialVersionUID = -7832219582908962549L
    }
}