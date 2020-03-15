using com.github.fernthedev.lightchat.core;
using com.github.fernthedev.lightchat.core.packets;
using DotNetty.Transport.Channels;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Channels;

namespace com.github.fernthedev.lightchat.client
{
    public class ClientHandler : ChannelHandlerAdapter
    {

        protected EventListener listener;
        protected Client client;

        public ClientHandler(Client client, EventListener listener)
        {
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
    public override void ChannelRegistered(IChannelHandlerContext ctx) {
/*            client.getPluginManager().callEvent(new ServerConnectFinishEvent(ctx.channel()));*/
        base.ChannelRegistered(ctx);
    }

    public override void ChannelRead(IChannelHandlerContext ctx, object msg) {
        Tuple<Packet, int> packet;

        StaticHandler.getCore().getLogger().debug("Received the packet {} from {}", msg.getClass().getName(), ctx.channel());

        if (msg instanceof Pair) {
            packet = (Pair<Packet, Integer>)msg;

            listener.received(packet.getKey(), packet.getValue());

        }

        super.channelRead(ctx, msg);
    }



    public override void ChannelUnregistered(ChannelHandlerContext ctx) throws Exception {
        client.getLoggerInterface().info("Lost connection to server.");

        client.disconnect(ServerDisconnectEvent.DisconnectStatus.CONNECTION_LOST);

        super.channelUnregistered(ctx);
    }

    @Override
    public override void ExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
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
