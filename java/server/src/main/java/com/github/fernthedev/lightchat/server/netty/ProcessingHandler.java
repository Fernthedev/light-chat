package com.github.fernthedev.lightchat.server.netty;

import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket;
import com.github.fernthedev.lightchat.core.packets.latency.LatencyPacket;
import com.github.fernthedev.lightchat.server.ClientConnection;
import com.github.fernthedev.lightchat.server.EventListener;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.event.PlayerDisconnectEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

@ChannelHandler.Sharable
public class ProcessingHandler extends ChannelInboundHandlerAdapter {

    private final Server server;

    public ProcessingHandler(Server server) {
        this.server = server;
    }

    /**
     * Calls {@link ChannelHandlerContext#fireChannelRegistered()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (validateIsBanned(ctx)) return;

        super.channelRegistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        close(ctx.channel());

        super.channelInactive(ctx);
        //clientConnection.close();
    }

    /**
     * Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to forward
     * to the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx   Channel
     * @param cause Cause of error.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (validateIsBanned(ctx)) return;

        if (cause instanceof IOException || cause.getCause() instanceof IOException) {
            server.getLogger().info("The channel {} has been closed from the client side.", ctx.channel());

            ClientConnection clientConnection = server.getPlayerHandler().getChannelMap().get(ctx.channel());

            if (clientConnection == null) return;

            if (clientConnection.getName() != null && clientConnection.isRegistered()) {
                server.getPluginManager().callEvent(new PlayerDisconnectEvent(clientConnection));
                server.logInfo("[{}] has disconnected from the server", clientConnection.getName());
            }

            if (server.getPlayerHandler().getUuidMap().containsValue(clientConnection)) {


                server.getPlayerHandler().getUuidMap().remove(clientConnection.getUuid());
                clientConnection.close();
            }
        } else super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (validateIsBanned(ctx)) return;

        try {
            ClientConnection connection = server.getPlayerHandler().getChannelMap().get(ctx.channel());
            EventListener eventListener = connection.getEventListener();

            if (msg instanceof ByteBuf && !server.getPlayerHandler().getChannelMap().containsKey(ctx.channel())) {
                // Discard the received data silently.
                ((ByteBuf) msg).release();
            }

            if (msg instanceof Pair) {
                Pair<? extends Packet, Integer> pair = (Pair<? extends Packet, Integer>) msg;
                if (pair.getKey() instanceof ConnectedPacket) {
                    if (!connection.isRegistered()) {
                        eventListener.handleConnect((ConnectedPacket) pair.getKey());
                    } else {
                        server.getLogger().warn("Connection {} just attempted to send a connection packet while registered. Glitch or security bug? ", connection.toString());
                    }
                } else {
                    if (server.getPlayerHandler().getChannelMap().containsKey(ctx.channel())) {
                        if (pair.getKey() != null) {
                            if (!(pair.getLeft() instanceof LatencyPacket))
                                StaticHandler.getCore().getLogger().debug("Received the packet {} from {}", pair.getLeft().getPacketName(), ctx.channel());

                            eventListener.received(pair.getKey(), pair.getRight());
                        }
                    }
                }
            }


        } finally {
            ReferenceCountUtil.release(msg);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (validateIsBanned(ctx)) return;

        ctx.flush();
        ctx.fireChannelReadComplete();
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Server.getLogger().info("Channel Registering");

        if (validateIsBanned(ctx)) return;

        Channel channel = ctx.channel();

        if (channel != null) {

            server.getLogger().debug("Channel active {}", channel.remoteAddress().toString());

            UUID uuid = UUID.randomUUID();

            // Prevent duplicate UUIDs
            while (server.getPlayerHandler().getUuidMap().containsKey(uuid)) {
                uuid = UUID.randomUUID();
            }

            ClientConnection clientConnection = new ClientConnection(server, channel, uuid, (clientConnection1) -> {
                clientConnection1.sendObject(new InitialHandshakePacket(clientConnection1.getTempKeyPair().getPublic(), StaticHandler.getVERSION_DATA()), false);

                server.getLogger().info("[{}] established", clientConnection1.getAddress());
            });

            //Server.getLogger().info("Registering " + clientConnection.getNameAddress());

            server.getPlayerHandler().getChannelMap().put(channel, clientConnection);

            server.getLogger().debug("Awaiting RSA key generation for packet registration");
        } else {
            server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }

        super.channelActive(ctx);
    }

    protected boolean validateIsBanned(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();

            if (server.getBanManager().isBanned(address.getAddress().getHostAddress())) {

                server.getLogger().debug("Closing connection because it is banned for {}", address.toString());
                close(ctx.channel());

                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    protected void close(Channel channel) {

        ClientConnection clientConnection = server.getPlayerHandler().getChannelMap().get(channel);

        if (clientConnection == null) {
            channel.close();

            return;
        }

        if (server.getPlayerHandler().getUuidMap().containsValue(clientConnection)) {
            server.getPlayerHandler().getUuidMap().remove(clientConnection.getUuid());
        }

        if (server.getPlayerHandler().getChannelMap().containsValue(clientConnection)) {
            server.getPlayerHandler().getChannelMap().remove(clientConnection.getChannel());
        }

        clientConnection.close();
    }

}
