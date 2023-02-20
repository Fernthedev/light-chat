package com.github.fernthedev.lightchat.client.event

import com.github.fernthedev.lightchat.core.api.Event
import com.github.fernthedev.lightchat.core.packets.IllegalConnectionPacket
import io.netty.channel.*

/**
 * Called when client has intentionally closed connection
 * May also be called when unintentional connections are closed
 *
 * Called on [Client.disconnect] after the connections are closed
 *
 */
class ServerDisconnectEvent(
    val channel: Channel,
    val disconnectStatus: DisconnectStatus, isAsynchronous: Boolean = false
) : Event(isAsynchronous) {


    enum class DisconnectStatus {
        /**
         *
         */
        DISCONNECTED,

        /**
         * The connection has been lost
         */
        CONNECTION_LOST,

        /**
         * When the server sends an [IllegalConnectionPacket]
         */
        ILLEGAL_CONNECTION,

        /**
         * Connection timed out
         */
        TIMEOUT,

        /**
         * Connection received exception
         */
        EXCEPTION
    }

}