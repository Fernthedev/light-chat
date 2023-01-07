package com.github.fernthedev.lightchat.core.api.event.api

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
    private var name: String? = null

    /**
     * This constructor is used to explicitly declare an event as synchronous
     * or asynchronous.
     *
     * @param isAsynchronous true indicates the event will fire asynchronously, false
     * by default from default constructor
     */
    val eventName: String?
        /**
         * Convenience method for providing a user-friendly identifier. By
         * default, it is the event's class's [ simple name][Class.getSimpleName].
         *
         * @return name of this event
         */
        get() {
            if (name == null) {
                name = javaClass.simpleName
            }
            return name
        }
    abstract val handlers: HandlerList

    enum class Result {
        /**
         * Deny the event. Depending on the event, the action indicated by the
         * event will either not take place or will be reverted. Some actions
         * may not be denied.
         */
        DENY,

        /**
         * Neither deny nor allow the event. The server will proceed with its
         * normal handling.
         */
        DEFAULT,

        /**
         * Allow / Force the event. The action indicated by the event will
         * take place if possible, even if the server would not normally allow
         * the action. Some actions may not be allowed.
         */
        ALLOW
    }
}