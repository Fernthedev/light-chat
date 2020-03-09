package com.github.fernthedev.client.netty;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.EventListener;
import com.github.fernthedev.client.event.ServerConnectFinishEvent;
import com.github.fernthedev.client.event.ServerDisconnectEvent;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.packets.Packet;
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
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        client.getPluginManager().callEvent(new ServerConnectFinishEvent(ctx.channel()));
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Pair<Packet, Integer> packet;

        StaticHandler.getCore().getLogger().debug("Received the packet {} from {}", msg.getClass().getName(), ctx.channel());

        if (msg instanceof Pair) {
            packet = (Pair<Packet, Integer>) msg;

            listener.received(packet.getKey(), packet.getValue());

        }

        super.channelRead(ctx, msg);
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        client.getLoggerInterface().info("Lost connection to server.");

        client.disconnect(ServerDisconnectEvent.DisconnectStatus.CONNECTION_LOST);

        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            client.getLoggerInterface().info("Timed out connection");

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
