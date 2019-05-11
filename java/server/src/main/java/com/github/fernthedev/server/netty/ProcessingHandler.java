package com.github.fernthedev.server.netty;

import com.github.fernthedev.packets.ConnectedPacket;
import com.github.fernthedev.packets.RequestInfoPacket;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.EventListener;
import com.github.fernthedev.server.PlayerHandler;
import com.github.fernthedev.server.Server;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.SealedObject;
import java.util.UUID;

@ChannelHandler.Sharable
    public class ProcessingHandler extends SimpleChannelInboundHandler<GeneratedMessageV3> {




    private Server server;

    public ProcessingHandler(Server server) {
        this.server = server;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientPlayer clientPlayer = Server.socketList.get(ctx.channel());

        if(PlayerHandler.players.containsValue(clientPlayer) && ctx.isRemoved()) {


            PlayerHandler.players.remove(clientPlayer.getId());
            clientPlayer.close();
        }

        //clientPlayer.close();
    }

    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p>
     * Is called for each message of type {@link GeneratedMessageV3}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GeneratedMessageV3 msg) throws Exception {
        try {

            EventListener eventListener = Server.socketList.get(ctx.channel()).getEventListener();

            if(msg instanceof ConnectedPacket) {
                eventListener.handleConnect((ConnectedPacket) msg);
            }else {
                if ((Object) msg instanceof SealedObject) {
                    SealedObject requestData = (SealedObject) (Object) msg;

                    GeneratedMessageV3 packet = (GeneratedMessageV3) Server.socketList.get(ctx.channel()).decryptObject(requestData);

                    eventListener.received(packet);


                } else if (msg != null) {
                    eventListener.received (msg);
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

       Server.getLogger().info("Channel Registering");
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

            ctx.writeAndFlush(RequestInfoPacket.newBuilder().setEncryptionKey(clientPlayer.getServerKey()));

        }else{
            Server.getLogger().info("Channel is null");
            throw new NullPointerException();
        }
    }

}
