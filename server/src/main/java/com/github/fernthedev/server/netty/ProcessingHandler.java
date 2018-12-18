package com.github.fernthedev.server.netty;

import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.server.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

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

        Packet requestData = (Packet) msg;

        if(Server.socketList.containsKey(ctx.channel())) {
            EventListener eventListener = new EventListener(server, Server.socketList.get(ctx.channel()));

            for (Object packetLos : packetsLost) {
                eventListener.recieved(packetLos);
            }

            eventListener.recieved(requestData);

            ctx.flush();
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

            ClientPlayer clientPlayer = new ClientPlayer(channel);

            //Server.getLogger().info("Registering " + clientPlayer.getNameAddress());


            Server.socketList.put(channel,clientPlayer);



            EventListener listener = new EventListener(server, clientPlayer);


            // And From your main() method or any other method
            FernThread runningFernThread;


            ServerThread serverThread = new ServerThread(server, channel, clientPlayer, listener);

            runningFernThread = new FernThread(serverThread);
            clientPlayer.setThread(serverThread);

            runningFernThread.startThread();

        }else{
            Server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }
    }

}
