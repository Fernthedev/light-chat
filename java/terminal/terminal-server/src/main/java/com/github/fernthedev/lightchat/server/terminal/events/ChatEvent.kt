package com.github.fernthedev.lightchat.server.terminal.events

import    com.github.fernthedev.lightchat.core.api.Cancellable
import com.github.fernthedev.lightchat.core.api.Event
import com.github.fernthedev.lightchat.server.SenderInterface

class ChatEvent(
    val sender: SenderInterface,
    var message: String,
    val isCommand: Boolean,
    async: Boolean = false,
) : Event(async), Cancellable {
    /**
     * Gets the cancellation state of this com.github.fernthedev.client.event. A cancelled com.github.fernthedev.client.event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this com.github.fernthedev.client.event is cancelled
     */
    /**
     * Sets the cancellation state of this com.github.fernthedev.client.event. A cancelled com.github.fernthedev.client.event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this com.github.fernthedev.client.event
     */
    override var isCancelled = false
}