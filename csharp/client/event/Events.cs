using com.github.fernthedev.lightchat.core.events;
using DotNetty.Transport.Channels;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.client.events
{



    /**
     * Called when client has successfully established a secure and valid connection
     */
    public class ServerConnectFinishEvent : IEvent
    {
        private IChannel Channel { get; }

        public ServerConnectFinishEvent(IChannel channel, bool async = false) : base(async)
        {
            this.Channel = channel;
        }
    }

    /**
     * Called when client has successfully established a secure and valid connection
     */
    public class ServerConnectHandshakeEvent : IEvent
    {
        private IChannel Channel { get; }

        public ServerConnectHandshakeEvent(IChannel channel, bool async = false) : base(async)
        {
            this.Channel = channel;
        }
    }

    /**
     * Called when client has intentionally closed connection
     * May also be called when unintentional connections are closed
     *
     * Called on {@link Client#disconnect()}
     *
     */
    public class ServerDisconnectEvent : IEvent
    {


        private IChannel Channel { get; }


        private DisconnectStatus disconnectStatus { get; }

        public ServerDisconnectEvent(IChannel channel, DisconnectStatus disconnectStatus, bool async = false) : base(async)
        {
            this.Channel = channel;
            this.disconnectStatus = disconnectStatus;
        }
        public enum DisconnectStatus
        {
            /**
             *
             */
            DISCONNECTED,

            /**
             * The connection has been lost
             */
            CONNECTION_LOST,

            /**
             * When the server sends an {@link IllegalConnectionPacket}
             */
            ILLEGAL_CONNECTION,

            /**
             * Connection timed out
             */
            TIMEOUT,

            /**
             * Connection received exception
             */
            EXCEPTION,
        }
    }
}
