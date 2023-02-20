package com.github.fernthedev.lightchat.server.event

import com.github.fernthedev.lightchat.core.api.Cancellable
import com.github.fernthedev.lightchat.server.security.AuthenticationManager.PlayerInfo

/**
 * Called when client has successfully established a secure and valid connection
 */
class AuthenticationAttemptedEvent : AuthenticateEvent, Cancellable {
    var eventStatus: EventStatus

    constructor(playerInfo: PlayerInfo, eventStatus: EventStatus) : super(playerInfo) {
        this.eventStatus = eventStatus
    }

    constructor(playerInfo: PlayerInfo, async: Boolean, eventStatus: EventStatus) : super(playerInfo, async) {
        this.eventStatus = eventStatus
    }

    enum class EventStatus {
        SUCCESS, ATTEMPT_FAILED, NO_MORE_TRIES
    }
}