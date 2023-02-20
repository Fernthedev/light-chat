package com.github.fernthedev.lightchat.core.api

/**
 * Represents an event.
 *
 * All events require a static method named getHandlerList() which returns the same [HandlerList] as [.getHandlers].
 *
 */
abstract class Event
/**
 * The default constructor is defined for cleaner code. This constructor
 * assumes the event is synchronous.
 */ @JvmOverloads constructor(
    /**
     * Any custom event that should not by synchronized with other events must
     * use the specific constructor. These are the caveats of using an
     * asynchronous event:
     *
     *  * The event is never fired from inside code triggered by a
     * synchronous event. Attempting to do so results in an [     ].
     *  * However, asynchronous event handlers may fire synchronous or
     * asynchronous events
     *  * The event may be fired multiple times simultaneously and in any
     * order.
     *  * Any newly registered or unregistered handler is ignored after an
     * event starts execution.
     *  * The handlers for this event may block for any length of time.
     *  * Some implementations may selectively declare a specific event use
     * as asynchronous. This behavior should be clearly defined.
     *  * Asynchronous calls are not calculated in the plugin timing system.
     *
     *
     * @return false by default, true if the event fires asynchronously
     */
    val isAsynchronous: Boolean = false
) {

}