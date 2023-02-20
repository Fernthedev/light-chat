package com.github.fernthedev.lightchat.server.netty

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket
import com.github.fernthedev.lightchat.core.packets.latency.LatencyPacket
import com.github.fernthedev.lightchat.server.*
import com.github.fernthedev.lightchat.server.event.PlayerDisconnectEvent
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetSocketAddress
import java.util.*

@Sharable
class ProcessingHandler(private val server: Server) : ChannelInboundHandlerAdapter() {
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
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        if (validateIsBanned(ctx)) return
        super.channelRegistered(ctx)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        close(ctx.channel())
        super.channelInactive(ctx)
        //clientConnection.close();
    }

    /**
     * Calls [ChannelHandlerContext.fireExceptionCaught] to forward
     * to the next [ChannelHandler] in the [ChannelPipeline].
     *
     *
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx   Channel
     * @param cause Cause of error.
     */
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (validateIsBanned(ctx)) return
        if (cause is IOException || cause.cause is IOException) {
            server.logger.info("The channel {} has been closed from the client side.", ctx.channel())
            val clientConnection = server.playerHandler.channelMap[ctx.channel()] ?: return
            if (server.playerHandler.uuidMap.containsValue(clientConnection)) {
                server.playerHandler.uuidMap.remove(clientConnection.uuid)
            }
            close(ctx.channel())
        } else super.exceptionCaught(ctx, cause)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (validateIsBanned(ctx)) return


        runBlocking {
            try {
                val connection: ClientConnection = server.playerHandler.channelMap[ctx.channel()]!!
                val eventListener = connection.eventListener
                if (!server.playerHandler.channelMap.containsKey(ctx.channel())) {
                    // Discard the received data silently.
                    ReferenceCountUtil.release(msg)
                    return@runBlocking
                }

                if (msg !is PacketTransporter) return@runBlocking

                val packet = msg.packet
                when {
                    packet is ConnectedPacket -> {
                        if (!connection.registered) {
                            eventListener.handleConnect(packet)
                        } else {
                            server.logger.warn(
                                "Connection {} just attempted to send a connection packet while registered. Glitch or security bug? ",
                                connection.toString()
                            )
                        }
                    }

                    packet is PacketJSON &&
                            server.playerHandler.channelMap.containsKey(ctx.channel()) -> {
                        if (msg.packet !is LatencyPacket) StaticHandler.core.logger.debug(
                            "Received the packet {} from {}", msg.packet
                                .packetName, ctx.channel()
                        )
                        eventListener.received(packet, msg.id)
                    }
                }
            } finally {
                ReferenceCountUtil.release(msg)
            }
        }
        super.channelRead(ctx, msg)
    }

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        if (validateIsBanned(ctx)) return
        ctx.flush()
        super.channelReadComplete(ctx)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        // Server.getLogger().info("Channel Registering");
        if (validateIsBanned(ctx)) return
        val channel = ctx.channel()


        if (channel != null) {
            server.logger.debug("Channel active {}", channel.remoteAddress().toString())
            var uuid = UUID.randomUUID()

            // Prevent duplicate UUIDs
            while (server.playerHandler.uuidMap.containsKey(uuid)) {
                uuid = UUID.randomUUID()
            }


            val clientConnection = ClientConnection(server, channel, uuid)

            //Server.getLogger().info("Registering " + clientConnection.getNameAddress());
            server.playerHandler.channelMap[channel] = clientConnection
            server.logger.debug("Awaiting RSA key generation for packet registration")

            val keyJob = server.rsaKeyThread.randomKey

            runBlocking {
                launch {
                    val key = keyJob.await()
                    clientConnection.setupKeypair(key)
                    val packet = InitialHandshakePacket(
                        key.public,
                        StaticHandler.VERSION_DATA
                    )
                    clientConnection.sendPacketLaunch(
                        packet.transport(
                            false
                        )
                    )

                    server.logger.info("[{}] established", clientConnection.address)
                }
            }
        } else {
            server.logger.info("Channel is null")
            throw NullPointerException()
        }
        super.channelActive(ctx)
    }

    protected fun validateIsBanned(ctx: ChannelHandlerContext): Boolean {
        if (ctx.channel().remoteAddress() is InetSocketAddress) {
            val address = ctx.channel().remoteAddress() as InetSocketAddress
            return if (server.banManager.isBanned(address.address.hostAddress)) {
                server.logger.debug("Closing connection because it is banned for {}", address)
                close(ctx.channel())
                true
            } else {
                false
            }
        }
        return false
    }

    protected fun close(channel: Channel) {
        val clientConnection = server.playerHandler.channelMap[channel]
        if (clientConnection == null) {
            channel.close()
            return
        }
        if (server.playerHandler.uuidMap.containsValue(clientConnection)) {
            server.playerHandler.uuidMap.remove(clientConnection.uuid)
        }
        if (server.playerHandler.channelMap.containsValue(clientConnection)) {
            server.playerHandler.channelMap.remove(clientConnection.channel)
        }
        if (clientConnection.registered) {
            server.eventHandler.callEvent(PlayerDisconnectEvent(clientConnection))
            server.logInfo("[{}] has disconnected from the server", clientConnection.name)
        }
        clientConnection.close()
    }
}