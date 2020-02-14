package com.github.fernthedev.server.netty;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.core.packets.handshake.InitialHandshakePacket;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.EventListener;
import com.github.fernthedev.server.PlayerHandler;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.event.PlayerDisconnectEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.UUID;

@ChannelHandler.Sharable
public class ProcessingHandler extends ChannelInboundHandlerAdapter {

    private Server server;

    public ProcessingHandler(Server server) {
        this.server = server;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ClientPlayer clientPlayer = PlayerHandler.getChannelMap().get(ctx.channel());

        if (clientPlayer == null) return;

        if (PlayerHandler.getUuidMap().containsValue(clientPlayer) && ctx.isRemoved()) {


            PlayerHandler.getUuidMap().remove(clientPlayer.getUuid());
            clientPlayer.close();
        }

        //clientPlayer.close();
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

            ClientPlayer clientPlayer = PlayerHandler.getChannelMap().get(ctx.channel());

            if (clientPlayer == null) return;

            if (clientPlayer.getName() != null && clientPlayer.isRegistered()) {
                server.getPluginManager().callEvent(new PlayerDisconnectEvent(clientPlayer));
                server.logInfo("[{}] has disconnected from the server", clientPlayer.getName());
            }

            if (PlayerHandler.getUuidMap().containsValue(clientPlayer)) {


                PlayerHandler.getUuidMap().remove(clientPlayer.getUuid());
                clientPlayer.close();
            }
        } else super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        try {

            EventListener eventListener = PlayerHandler.getChannelMap().get(ctx.channel()).getEventListener();

            if (msg instanceof ConnectedPacket) {
                eventListener.handleConnect((ConnectedPacket) msg);
            } else {
                if (!PlayerHandler.getChannelMap().containsKey(ctx.channel())) {
                    // Discard the received data silently.
                    ((ByteBuf) msg).release();

                } else if (msg instanceof Packet) {
                    StaticHandler.getCore().getLogger().debug("Received the packet {} from {}", ((Packet) msg).getPacketName(), ctx.channel());
                    eventListener.received((Packet) msg);
                }
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (!ctx.channel().isActive()) {
            ctx.close();
        }

        // Server.getLogger().info("Channel Registering");
        Channel channel = ctx.channel();

        if (channel != null) {
            UUID uuid = UUID.randomUUID();

            // Prevent duplicate UUIDs
            while (PlayerHandler.getUuidMap().containsKey(uuid)) {
                uuid = UUID.randomUUID();
            }

            ClientPlayer clientPlayer = new ClientPlayer(server, channel, uuid);

            //Server.getLogger().info("Registering " + clientPlayer.getNameAddress());

            PlayerHandler.getChannelMap().put(channel, clientPlayer);

            clientPlayer.awaitKeys();

            clientPlayer.sendObject(new InitialHandshakePacket(clientPlayer.getTempKeyPair().getPublic(), StaticHandler.getVERSION_DATA()), false);

            Server.getLogger().info("[{}] established", clientPlayer.getAddress());
        } else {
            Server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }
    }

}
