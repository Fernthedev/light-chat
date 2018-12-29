package com.github.fernthedev.client.netty;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.EventListener;
import com.github.fernthedev.packets.ConnectedPacket;
import com.github.fernthedev.packets.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;

@ChannelHandler.Sharable
public class ClientHandler extends ChannelHandlerAdapter {

    protected EventListener listener;

    public ClientHandler(Client client,EventListener listener) {
        this.listener = listener;
        this.client = client;
        String os = client.getOSName();
        connectedPacket = new ConnectedPacket(client.name,os);
    }

    protected Client client;
    protected ConnectedPacket connectedPacket;

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        client.registered = true;


        ctx.writeAndFlush(connectedPacket);
        Client.getLogger().log(Level.FINE,"Sent connect packet for request");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        if(msg instanceof Packet) {
            Packet packet = (Packet) msg;
            listener.recieved(packet);

            /*if (!(msg instanceof PingPacket))
                client.getLogger().info("Packet received which is " + msg);*/
        }

        //ctx.close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Client.getLogger().info("Lost connection to server.");
        client.getClientThread().close();
    }
}
