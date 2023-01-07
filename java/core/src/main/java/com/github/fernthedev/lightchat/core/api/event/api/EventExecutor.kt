package com.github.fernthedev.lightchat.core.api.event.api

/**
 * Interface which defines the class for com.github.fernthedev.client.event call backs to plugins
 */
interface EventExecutor {
    @Throws(EventException::class)
    fun execute(listener: Listener, event: Event)
}