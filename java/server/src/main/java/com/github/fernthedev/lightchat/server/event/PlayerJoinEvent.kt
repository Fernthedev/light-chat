package com.github.fernthedev.lightchat.server.event

import com.github.fernthedev.lightchat.core.api.Cancellable
import com.github.fernthedev.lightchat.core.api.Event
import com.github.fernthedev.lightchat.server.ClientConnection

/**
 * Called when client has successfully established a secure and valid connection
 */
class PlayerJoinEvent(
    val joinPlayer: ClientConnection, async: Boolean,
    override var isCancelled: Boolean = false,
) : Event(async),
    Cancellable {

}