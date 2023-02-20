package com.github.fernthedev.lightchat.client.event

import com.github.fernthedev.lightchat.core.api.Event
import io.netty.channel.Channel

/**
 * Called when client has successfully established a secure and valid connection
 */
class ServerConnectHandshakeEvent(
    val channel: Channel, isAsynchronous: Boolean = false
) : Event(isAsynchronous) {



}