package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import io.netty.channel.ChannelFuture
import kotlinx.coroutines.*
import java.io.Serializable

class Console(
    @Transient val server: Server
) : SenderInterface, Serializable {
    override suspend fun sendPacketDeferred(transporter: PacketTransporter): Deferred<ChannelFuture> = coroutineScope {
        return@coroutineScope async {
            null!!
        }
    }

    override suspend fun sendPacketLaunch(transporter: PacketTransporter): Job = coroutineScope {
        return@coroutineScope launch { }
    }


    override val name: String
        get() = server.name

    companion object {
        private const val serialVersionUID = -7832219582908962549L
    }
}