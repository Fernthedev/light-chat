package com.github.fernthedev.lightchat.server.event

import com.github.fernthedev.lightchat.core.api.Cancellable
import com.github.fernthedev.lightchat.core.api.Event

/**
 * Called when client has successfully established a secure and valid connection
 */
class BanEvent(
    /**
     * If true, player was banned
     * if false, player was unbanned
     */
    val banned: Boolean,
    val bannedIP: String,
    override var isCancelled: Boolean = false,
    isAsynchronous: Boolean = false
) : Event(isAsynchronous), Cancellable {

}