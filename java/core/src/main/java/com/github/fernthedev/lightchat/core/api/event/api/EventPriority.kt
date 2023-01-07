package com.github.fernthedev.lightchat.core.api.event.api

/**
 * Represents an com.github.fernthedev.client.event's priority in execution
 */
enum class EventPriority(val slot: Int) {
    /**
     * Event call is of very low importance and should be ran first, to allow
     * other plugins to further customise the outcome
     */
    LOWEST(0),

    /**
     * Event call is of low importance
     */
    LOW(1),

    /**
     * Event call is neither important nor unimportant, and may be ran
     * normally
     */
    NORMAL(2),

    /**
     * Event call is of high importance
     */
    HIGH(3),

    /**
     * Event call is critical and must have the final say in what happens
     * to the com.github.fernthedev.client.event
     */
    HIGHEST(4),

    /**
     * Event is listened to purely for monitoring the outcome of an com.github.fernthedev.client.event.
     *
     *
     * No modifications to the com.github.fernthedev.client.event should be made under this priority
     */
    MONITOR(5)

}