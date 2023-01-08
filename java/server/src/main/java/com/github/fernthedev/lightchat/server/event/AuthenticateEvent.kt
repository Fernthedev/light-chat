package com.github.fernthedev.lightchat.server.event

import com.github.fernthedev.lightchat.core.api.event.api.Cancellable
import com.github.fernthedev.lightchat.core.api.event.api.Event
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList
import com.github.fernthedev.lightchat.server.security.AuthenticationManager.PlayerInfo

/**
 * Called when client has successfully established a secure and valid connection
 */
open class AuthenticateEvent(val playerInfo: PlayerInfo, async: Boolean = false,
                             override var isCancelled: Boolean = false,
                             override val handlers: HandlerList = handlerList
) : Event(async),
    Cancellable {

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}