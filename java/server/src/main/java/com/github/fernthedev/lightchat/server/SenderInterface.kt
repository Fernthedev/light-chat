package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import io.netty.channel.ChannelFuture
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

interface SenderInterface {

    suspend fun sendPacketDeferred(transporter: PacketTransporter): Deferred<ChannelFuture>
    suspend fun sendPacketLaunch(transporter: PacketTransporter): Job

    fun sendPacketBlocking(transporter: PacketTransporter): ChannelFuture {
        return runBlocking {
            return@runBlocking sendPacketDeferred(transporter).await()
        }
    }

    val name: String
}