package com.github.fernthedev.lightchat.server.netty

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket
import com.github.fernthedev.lightchat.core.packets.latency.LatencyPacket
import com.github.fernthedev.lightchat.server.*
import com.github.fernthedev.lightchat.server.event.PlayerDisconnectEvent
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.tuple.Pair
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
                if (msg is ByteBuf && !server.playerHandler.channelMap.containsKey(ctx.channel())) {
                    // Discard the received data silently.
                    msg.release()
                }
                if (msg is Pair<*, *>) {
                    val pair = msg as Pair<out Packet?, Int>
                    if (pair.key is ConnectedPacket) {
                        if (!connection.registered) {
                            eventListener.handleConnect(pair.key as ConnectedPacket)

                        } else {
                            server.logger.warn(
                                "Connection {} just attempted to send a connection packet while registered. Glitch or security bug? ",
                                connection.toString()
                            )
                        }
                    } else {
                        if (server.playerHandler.channelMap.containsKey(ctx.channel())) {
                            if (pair.key != null) {
                                if (pair.left !is LatencyPacket) StaticHandler.core.logger.debug(
                                    "Received the packet {} from {}", pair.left!!
                                        .packetName, ctx.channel()
                                )
                                eventListener.received(pair.key!!, pair.right)
                            }
                        }
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
            val clientConnection = ClientConnection(server, channel, uuid) { clientConnection1: ClientConnection ->
                clientConnection1.tempKeyPair?.let {
                    InitialHandshakePacket(
                        it.public,
                        StaticHandler.VERSION_DATA
                    )
                }?.let {
                    clientConnection1.sendObject(
                        it.transport(
                            false
                        )
                    )
                }
                server.logger.info("[{}] established", clientConnection1.address)
            }

            //Server.getLogger().info("Registering " + clientConnection.getNameAddress());
            server.playerHandler.channelMap[channel] = clientConnection
            server.logger.debug("Awaiting RSA key generation for packet registration")
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
            server.pluginManager.callEvent(PlayerDisconnectEvent(clientConnection))
            server.logInfo("[{}] has disconnected from the server", clientConnection.name)
        }
        clientConnection.close()
    }
}