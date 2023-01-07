package com.github.fernthedev.lightchat.core.api.event.api

import com.github.fernthedev.lightchat.core.api.plugin.*

/**
 * Stores relevant information for plugin listeners
 */
class RegisteredListener(
    /**
     * Gets the listener for this registration
     *
     * @return Registered Listener
     */
    val listener: Listener, private val executor: EventExecutor, plugin: Plugin?,
    /**
     * Gets the priority for this registration
     *
     * @return Registered Priority
     */
    val priority: EventPriority,
    /**
     * Whether this listener accepts cancelled events
     *
     * @return True when ignoring cancelled events
     */
    val isIgnoringCancelled: Boolean
) {
    val plugin: Plugin? = null

    /**
     * Calls the com.github.fernthedev.client.event executor
     *
     * @param event The com.github.fernthedev.client.event
     * @throws EventException If an com.github.fernthedev.client.event handler throws an exception.
     */
    @Throws(EventException::class)
    fun callEvent(event: Event) {
        if (event is Cancellable) {
            if ((event as Cancellable).isCancelled && isIgnoringCancelled) {
                return
            }
        }
        executor.execute(listener, event)
    }
}