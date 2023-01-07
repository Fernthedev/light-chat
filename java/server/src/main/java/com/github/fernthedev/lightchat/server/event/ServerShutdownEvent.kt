package com.github.fernthedev.lightchat.server.event

import com.github.fernthedev.lightchat.core.api.event.api.Event
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList

class ServerShutdownEvent(override val handlers: HandlerList = handlerList) : Event() {
    companion object {
        val handlerList = HandlerList()
    }
}