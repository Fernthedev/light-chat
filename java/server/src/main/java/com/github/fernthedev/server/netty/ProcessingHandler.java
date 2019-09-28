package com.github.fernthedev.server.netty;

import com.github.fernthedev.packets.ConnectedPacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.packets.RequestInfoPacket;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.EventListener;
import com.github.fernthedev.server.PlayerHandler;
import com.github.fernthedev.server.Server;
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

@ChannelHandler.Sharable
public class ProcessingHandler extends ChannelInboundHandlerAdapter {



    private List<Object> packetsLost = new ArrayList<>();

    private Server server;

    public ProcessingHandler(Server server) {this.server = server;}

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientPlayer clientPlayer = Server.socketList.get(ctx.channel());

        if(PlayerHandler.players.containsValue(clientPlayer) && ctx.isRemoved()) {


            PlayerHandler.players.remove(clientPlayer.getId());
            clientPlayer.close();
        }

        //clientPlayer.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        try {

            EventListener eventListener = Server.socketList.get(ctx.channel()).getEventListener();

            if(msg instanceof ConnectedPacket) {
                eventListener.handleConnect((ConnectedPacket) msg);
            }else {
                if (!Server.socketList.containsKey(ctx.channel())) {
                    // Discard the received data silently.
                    ((ByteBuf) msg).release();
                }else if (msg instanceof SealedObject) {
                    SealedObject requestData = (SealedObject) msg;

                    Packet packet = (Packet) Server.socketList.get(ctx.channel()).decryptObject(requestData);

                    eventListener.received(packet);


                } else if (msg instanceof Packet) {
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
            ClientPlayer clientPlayer = new ClientPlayer(server,channel,UUID.randomUUID());

            //Server.getLogger().info("Registering " + clientPlayer.getNameAddress());


            Server.socketList.put(channel,clientPlayer);

            ctx.writeAndFlush(new RequestInfoPacket(clientPlayer.getServerKey()));
            Server.getLogger().info("[{}] established",clientPlayer.getAdress());
        }else{
            Server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }
    }

}
