package com.github.fernthedev.server.netty;

import com.github.fernthedev.packets.ConnectedPacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.packets.RequestInfoPacket;
import com.github.fernthedev.server.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.SealedObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class ProcessingHandler extends ChannelInboundHandlerAdapter {



    private List<Object> packetsLost = new ArrayList<>();

    private Server server;

    public ProcessingHandler(Server server) {this.server = server;}

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ClientPlayer clientPlayer = Server.socketList.get(ctx.channel());

        if(PlayerHandler.players.containsValue(clientPlayer)) {


            PlayerHandler.players.remove(clientPlayer.getId());
            clientPlayer.close();
        }

        //clientPlayer.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        try {
            EventListener eventListener = new EventListener(server, Server.socketList.get(ctx.channel()));

            if (!Server.socketList.containsKey(ctx.channel())) {
                // Discard the received data silently.
                ((ByteBuf) msg).release();
            } else {
                if (msg instanceof SealedObject) {
                    SealedObject requestData = (SealedObject) msg;


                    //new Thread(() -> eventListener.received(requestData)).start();
                    long startTime = System.nanoTime();
                    long startTimeDecryption = System.nanoTime();

                    Packet packet = (Packet) Server.socketList.get(ctx.channel()).decryptObject(requestData);
                    Server.getLogger().info("Time to decrypt data was " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeDecryption) + "ms");


                    eventListener.received(packet);
                    Server.getLogger().info("Time to handle data was " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + "ms");

                } else if (msg instanceof ConnectedPacket) {

                    //new Thread(() -> eventListener.handleConnect((ConnectedPacket) msg)).start();
                    eventListener.handleConnect((ConnectedPacket) msg);

                } else if (msg instanceof Packet) {
                    long startTime = System.nanoTime();

                    eventListener.received((Packet) msg);

                    Server.getLogger().info("Time to handle data was " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + "ms");
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
        if(!ctx.channel().isActive()) {
            try {
                ctx.close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

       // Server.getLogger().info("Channel Registering");
        Channel channel = ctx.channel();

        if(Server.getInstance().getBanManager().isBanned(ctx.channel().remoteAddress().toString())) {
            ctx.flush();
            ctx.close();
            return;
        }

        if (channel != null) {
            Server server = Server.channelServerHashMap.get(ctx.channel());

            ClientPlayer clientPlayer = new ClientPlayer(channel,UUID.randomUUID());

            //Server.getLogger().info("Registering " + clientPlayer.getNameAddress());


            Server.socketList.put(channel,clientPlayer);



            EventListener listener = new EventListener(server, clientPlayer);


            // And From your main() method or any other method
            Thread runningFernThread;


            ServerThread serverThread = new ServerThread(server, channel, clientPlayer, listener);

            runningFernThread = new Thread(serverThread);
            clientPlayer.setThread(serverThread);

            runningFernThread.start();

            ctx.writeAndFlush(new RequestInfoPacket(clientPlayer.getServerKey()));

        }else{
            Server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }
    }

}
