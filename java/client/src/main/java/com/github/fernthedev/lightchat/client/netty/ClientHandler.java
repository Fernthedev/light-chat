package com.github.fernthedev.lightchat.client.netty;

import com.github.fernthedev.lightchat.client.Client;
import com.github.fernthedev.lightchat.client.EventListener;
import com.github.fernthedev.lightchat.client.event.ServerConnectFinishEvent;
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.packets.Packet;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {

    protected EventListener listener;

    protected Client client;

    public ClientHandler(Client client, EventListener listener) {
        this.listener = listener;
        this.client = client;
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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        client.getPluginManager().callEvent(new ServerConnectFinishEvent(ctx.channel()));
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Pair<Packet, Integer> packet;



        if (msg instanceof Pair) {
            packet = (Pair<Packet, Integer>) msg;

            StaticHandler.getCore().getLogger().debug("Received the packet {} from {}", packet.getKey().getPacketName(), ctx.channel());

            listener.received(packet.getKey(), packet.getValue());

        }

        super.channelRead(ctx, msg);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isActive()) {
            client.getLogger().info("Lost connection to server.");

            client.disconnect(ServerDisconnectEvent.DisconnectStatus.CONNECTION_LOST);
        }

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            client.getLogger().info("Timed out connection");

            if (StaticHandler.isDebug()) StaticHandler.getCore().getLogger().error(cause.getMessage(), cause);

            client.disconnect(ServerDisconnectEvent.DisconnectStatus.TIMEOUT);
        } else if (cause instanceof IOException) {
            client.disconnect(ServerDisconnectEvent.DisconnectStatus.EXCEPTION);
            cause.printStackTrace();
        } else {
            cause.printStackTrace();
        }

        super.exceptionCaught(ctx, cause);
    }
}
