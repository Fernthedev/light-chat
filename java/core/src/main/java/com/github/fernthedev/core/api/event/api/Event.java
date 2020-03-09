package com.github.fernthedev.core.api.event.api;

/**
 * Represents an com.github.fernthedev.client.event.
 *
 * All events require a static method named getHandlerList() which returns the same {@link HandlerList} as {@link #getHandlers()}.
 *
 */
public abstract class Event {
    private String name;
    private final boolean async;

    /**
     * The default constructor is defined for cleaner code. This constructor
     * assumes the com.github.fernthedev.client.event is synchronous.
     */
    public Event() {
        this(false);
    }

    /**
     * This constructor is used to explicitly declare an event as synchronous
     * or asynchronous.
     *
     * @param isAsync true indicates the com.github.fernthedev.client.event will fire asynchronously, false
     *     by default from default constructor
     */
    public Event(boolean isAsync) {
        this.async = isAsync;
    }

    /**
     * Convenience method for providing a user-friendly identifier. By
     * default, it is the com.github.fernthedev.client.event's class's {@linkplain Class#getSimpleName()
     * simple name}.
     *
     * @return name of this com.github.fernthedev.client.event
     */
    public String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }

    public abstract HandlerList getHandlers();

    /**
     * Any custom com.github.fernthedev.client.event that should not by synchronized with other events must
     * use the specific constructor. These are the caveats of using an
     * asynchronous com.github.fernthedev.client.event:
     * <ul>
     * <li>The com.github.fernthedev.client.event is never fired from inside code triggered by a
     *     synchronous com.github.fernthedev.client.event. Attempting to do so results in an {@link
     *     IllegalStateException}.
     * <li>However, asynchronous com.github.fernthedev.client.event handlers may fire synchronous or
     *     asynchronous events
     * <li>The com.github.fernthedev.client.event may be fired multiple times simultaneously and in any
     *     order.
     * <li>Any newly registered or unregistered handler is ignored after an
     *     com.github.fernthedev.client.event starts execution.
     * <li>The handlers for this com.github.fernthedev.client.event may block for any length of time.
     * <li>Some implementations may selectively declare a specific com.github.fernthedev.client.event use
     *     as asynchronous. This behavior should be clearly defined.
     * <li>Asynchronous calls are not calculated in the com.github.fernthedev.client.plugin timing system.
     * </ul>
     *
     * @return false by default, true if the com.github.fernthedev.client.event fires asynchronously
     */
    public final boolean isAsynchronous() {
        return async;
    }

    public enum Result {

        /**
         * Deny the com.github.fernthedev.client.event. Depending on the com.github.fernthedev.client.event, the action indicated by the
         * com.github.fernthedev.client.event will either not take place or will be reverted. Some actions
         * may not be denied.
         */
        DENY,
        /**
         * Neither deny nor allow the com.github.fernthedev.client.event. The server will proceed with its
         * normal handling.
         */
        DEFAULT,
        /**
         * Allow / Force the com.github.fernthedev.client.event. The action indicated by the com.github.fernthedev.client.event will
         * take place if possible, even if the server would not normally allow
         * the action. Some actions may not be allowed.
         */
        ALLOW;
    }
}
