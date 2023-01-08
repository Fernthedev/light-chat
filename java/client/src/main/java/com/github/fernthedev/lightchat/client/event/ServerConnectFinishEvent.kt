package com.github.fernthedev.lightchat.client.event

import com.github.fernthedev.lightchat.core.api.event.api.Event
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList
import io.netty.channel.Channel

/**
 * Called when client has successfully established a secure and valid connection
 */
class ServerConnectFinishEvent(
    val channel: Channel, isAsynchronous: Boolean = false,
    override val handlers: HandlerList = handlerList
) : Event(isAsynchronous) {


    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}