package com.github.fernthedev.client.event;


import com.github.fernthedev.client.Client;
import com.github.fernthedev.core.api.event.api.Event;
import com.github.fernthedev.core.api.event.api.HandlerList;
import com.github.fernthedev.core.packets.IllegalConnectionPacket;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Called when client has intentionally closed connection
 * May also be called when unintentional connections are closed
 *
 * Called on {@link Client#disconnect()}
 *
 */
@RequiredArgsConstructor
public class ServerDisconnectEvent extends Event  {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Channel channel;

    @Getter
    private final DisconnectStatus disconnectStatus;

    public ServerDisconnectEvent(Channel channel, DisconnectStatus disconnectStatus, boolean async) {
        super(async);
        this.channel = channel;
        this.disconnectStatus = disconnectStatus;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum DisconnectStatus {
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
