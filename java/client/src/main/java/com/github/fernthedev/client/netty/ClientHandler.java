package com.github.fernthedev.client.netty;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.EventListener;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.Getter;

@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {

    protected EventListener listener;

    protected Client client;

    @Getter
    protected ConnectedPacket connectedPacket;

    public ClientHandler(Client client, EventListener listener) {
        this.listener = listener;
        this.client = client;
        String os = client.getOSName();

        connectedPacket = new ConnectedPacket(client.getName(), os, StaticHandler.getVERSION_DATA());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet;

        StaticHandler.getCore().getLogger().debug("Received the packet {} from {}", msg.getClass().getName(), ctx.channel());

        if (msg instanceof Packet) {
            packet = (Packet) msg;

            listener.received(packet);

        }

        super.channelRead(ctx, msg);
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        client.getLoggerInterface().info("Lost connection to server.");
        client.close();
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            client.getLoggerInterface().info("Timed out connection");

            if (StaticHandler.isDebug()) StaticHandler.getCore().getLogger().error(cause.getMessage(), cause);

            client.close();
        } else {
            cause.printStackTrace();
        }
        super.exceptionCaught(ctx, cause);
    }
}
