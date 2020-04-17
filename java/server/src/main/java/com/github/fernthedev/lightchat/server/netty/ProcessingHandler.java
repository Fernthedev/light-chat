package com.github.fernthedev.server.netty;

import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket;
import com.github.fernthedev.server.ClientConnection;
import com.github.fernthedev.server.EventListener;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.event.PlayerDisconnectEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.UUID;

@ChannelHandler.Sharable
public class ProcessingHandler extends ChannelInboundHandlerAdapter {

    private Server server;

    public ProcessingHandler(Server server) {
        this.server = server;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientConnection clientConnection = server.getPlayerHandler().getChannelMap().get(ctx.channel());

        if (clientConnection == null) return;

        if (server.getPlayerHandler().getUuidMap().containsValue(clientConnection) && ctx.isRemoved()) {


            server.getPlayerHandler().getUuidMap().remove(clientConnection.getUuid());
            clientConnection.close();
        }

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

        if (cause instanceof IOException || cause.getCause() instanceof IOException) {
            Server.getLogger().info("The channel {} has been closed from the client side.", ctx.channel());

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

        try {

            EventListener eventListener = server.getPlayerHandler().getChannelMap().get(ctx.channel()).getEventListener();

            if (msg instanceof Pair) {
                Pair<? extends Packet, Integer> pair = (Pair<? extends Packet, Integer>) msg;
                if (pair.getKey() instanceof ConnectedPacket) {
                    eventListener.handleConnect((ConnectedPacket) pair.getKey());
                } else {
                    if (!server.getPlayerHandler().getChannelMap().containsKey(ctx.channel())) {
                        // Discard the received data silently.
                        ((ByteBuf) msg).release();

                    } else if (pair.getKey() != null) {
                        StaticHandler.getCore().getLogger().debug("Received the packet {} from {}", pair.getLeft().getPacketName(), ctx.channel());
                        eventListener.received(pair.getKey(), pair.getRight());
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
        ctx.flush();
        ctx.fireChannelReadComplete();
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isActive()) {
            ctx.close();
        }

        // Server.getLogger().info("Channel Registering");
        Channel channel = ctx.channel();

        if (channel != null) {
            UUID uuid = UUID.randomUUID();

            // Prevent duplicate UUIDs
            while (server.getPlayerHandler().getUuidMap().containsKey(uuid)) {
                uuid = UUID.randomUUID();
            }

            ClientConnection clientConnection = new ClientConnection(server, channel, uuid);

            //Server.getLogger().info("Registering " + clientConnection.getNameAddress());

            server.getPlayerHandler().getChannelMap().put(channel, clientConnection);

            clientConnection.onKeyGenerate(() -> {
                clientConnection.sendObject(new InitialHandshakePacket(clientConnection.getTempKeyPair().getPublic(), StaticHandler.getVERSION_DATA()), false);

                Server.getLogger().info("[{}] established", clientConnection.getAddress());
            });
        } else {
            Server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }

        super.channelActive(ctx);
    }

}
