package com.github.fernthedev.lightchat.server.event

import com.github.fernthedev.lightchat.core.api.event.api.HandlerList
import com.github.fernthedev.lightchat.server.security.AuthenticationManager.PlayerInfo

/**
 * Called when client has successfully established a secure and valid connection
 */
class AuthenticateRequestEvent : AuthenticateEvent {
    constructor(playerInfo: PlayerInfo) : super(playerInfo)
    constructor(playerInfo: PlayerInfo, async: Boolean) : super(playerInfo, async)

    companion object {
        val handlerList = HandlerList()
    }
}