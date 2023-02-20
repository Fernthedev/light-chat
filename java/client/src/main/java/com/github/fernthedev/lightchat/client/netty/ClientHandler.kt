package com.github.fernthedev.lightchat.client.netty

import com.github.fernthedev.lightchat.client.*
import com.github.fernthedev.lightchat.client.event.ServerConnectFinishEvent
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent.DisconnectStatus
import com.github.fernthedev.lightchat.core.StaticHandler.core
import com.github.fernthedev.lightchat.core.StaticHandler.isDebug
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.ReadTimeoutException
import kotlinx.coroutines.runBlocking
import java.io.IOException

@Sharable
open class ClientHandler(protected var client: Client, protected var listener: EventListener) :
    ChannelInboundHandlerAdapter() {
    /**
     * Calls [ChannelHandlerContext.fireChannelRegistered] to forward
     * to the next [ChannelInboundHandler] in the [ChannelPipeline].
     *
     *
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     */
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        client.eventHandler.callEvent(ServerConnectFinishEvent(ctx.channel()))
        super.channelActive(ctx)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is PacketTransporter) {
            core.logger.debug("Received the packet {} from {}", msg.packet.packetName, ctx.channel())
            runBlocking {
                val packet = msg.packet
                if (packet is PacketJSON) {
                    listener.received(packet, msg.id)
                }
            }
        }
        super.channelRead(ctx, msg)
    }

    /**
     * Calls [ChannelHandlerContext.fireChannelUnregistered] to forward
     * to the next [ChannelInboundHandler] in the [ChannelPipeline].
     *
     *
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     */
    @Throws(Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        if (!ctx.channel().isActive) {
            client.logger.info("Lost connection to server.")
            client.disconnect(DisconnectStatus.CONNECTION_LOST)
        }
        super.channelUnregistered(ctx)
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        when (cause) {
            is ReadTimeoutException -> {
                client.logger.info("Timed out connection")
                if (isDebug()) core.logger.error(cause.message, cause)
                client.disconnect(DisconnectStatus.TIMEOUT)
            }

            is IOException -> {
                client.disconnect(DisconnectStatus.EXCEPTION)
                cause.printStackTrace()
            }

            else -> {
                cause.printStackTrace()
            }
        }
        super.exceptionCaught(ctx, cause)
    }
}