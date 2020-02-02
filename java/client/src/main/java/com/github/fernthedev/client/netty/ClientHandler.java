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

import javax.crypto.SealedObject;

@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {

    protected EventListener listener;

    protected Client client;

    @Getter
    protected ConnectedPacket connectedPacket;

    public ClientHandler(Client client,EventListener listener) {
        this.listener = listener;
        this.client = client;
        String os = client.getOSName();

        connectedPacket = new ConnectedPacket(client.name, os, StaticHandler.getVERSION_DATA());
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        Packet packet;

      //  client.getLogger().info("Received " + msg);

        if(msg instanceof SealedObject) {

            SealedObject ob = (SealedObject) msg;

            //packet = (Packet) EncryptionHandler.decrypt(ob, client.getPrivateKey());
            long startTime = System.nanoTime();
            client.getLoggerInterface().error("Sealed object received, not expected.");
//            packet = (Packet) client.decryptObject(ob);

          //  client.getLogger().info("Decrypted object is " + packet + " took " + (System.nanoTime() - startTime) / 1000000 + "ms");

//            listener.received(packet);
        }else if (msg instanceof Packet){
            packet = (Packet) msg;

            listener.received(packet);
        }





        //ctx.close();
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        client.getLoggerInterface().info("Lost connection to server.");
        client.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if(cause instanceof ReadTimeoutException) {
            client.getLoggerInterface().info("Timed out connection");
            client.close();
        }else{
            cause.printStackTrace();
        }
    }
}
