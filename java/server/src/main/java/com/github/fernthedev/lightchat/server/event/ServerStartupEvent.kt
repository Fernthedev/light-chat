package com.github.fernthedev.lightchat.server.event

import com.github.fernthedev.lightchat.core.api.event.api.Event
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList

class ServerStartupEvent
/**
 * This constructor is used to explicitly declare an event as synchronous
 * or asynchronous.
 *
 * @param isAsync true indicates the com.github.fernthedev.client.event will fire asynchronously, false
 * by default from default constructor
 */
    (isAsync: Boolean = false, override val handlers: HandlerList = handlerList) : Event(isAsync) {

    companion object {
        val handlerList = HandlerList()
    }
}