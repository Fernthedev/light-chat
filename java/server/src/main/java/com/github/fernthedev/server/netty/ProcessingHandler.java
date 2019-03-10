package com.github.fernthedev.server.netty;

import com.github.fernthedev.packets.ConnectedPacket;
import com.github.fernthedev.packets.RequestInfoPacket;
import com.github.fernthedev.server.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.SealedObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ChannelHandler.Sharable
public class ProcessingHandler extends ChannelHandlerAdapter {



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

        new Thread(() -> {
            EventListener eventListener = new EventListener(server, Server.socketList.get(ctx.channel()));

            if (msg instanceof SealedObject) {
                SealedObject requestData = (SealedObject) msg;


                if (Server.socketList.containsKey(ctx.channel())) {


                    eventListener.received(requestData);
                    ctx.flush();

                }
            } else if (msg instanceof ConnectedPacket) {
                if (Server.socketList.containsKey(ctx.channel())) {
                    eventListener.handleConnect((ConnectedPacket) msg);
                    ctx.flush();
                }
            }
        }).start();



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
            FernThread runningFernThread;


            ServerThread serverThread = new ServerThread(server, channel, clientPlayer, listener);

            runningFernThread = new FernThread(serverThread);
            clientPlayer.setThread(serverThread);

            runningFernThread.startThread();

            ctx.writeAndFlush(new RequestInfoPacket(clientPlayer.getServerKey()));

        }else{
            Server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }
    }

}
