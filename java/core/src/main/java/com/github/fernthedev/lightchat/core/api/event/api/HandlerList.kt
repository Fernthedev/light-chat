package com.github.fernthedev.lightchat.core.api.event.api

import java.util.*

/**
 * A list of com.github.fernthedev.client.event handlers, stored per-com.github.fernthedev.client.event. Based on lahwran's fevents.
 */
class HandlerList {
    /**
     * Handler array. This field being an array is the key to this system's
     * speed.
     */
    @Volatile
    private var handlers: Array<RegisteredListener>? = null

    /**
     * Dynamic handler lists. These are changed using register() and
     * unregister() and are automatically baked to the handlers array any time
     * they have changed.
     */
    private val handlerslots: EnumMap<EventPriority, ArrayList<RegisteredListener>> = EnumMap(EventPriority::class.java)

    /**
     * Create a new handler list and initialize using EventPriority.
     *
     *
     * The HandlerList is then added to meta-list for use in bakeAll()
     */
    init {
        for (o in EventPriority.values()) {
            handlerslots[o] = ArrayList()
        }
        synchronized(allLists) { allLists.add(this) }
    }

    /**
     * Register a new listener in this handler list
     *
     * @param listener listener to register
     */
    @Synchronized
    fun register(listener: RegisteredListener) {
        check(!handlerslots[listener.priority]!!.contains(listener)) {
            "This listener is already registered to priority " + listener.priority.toString()
        }
        handlers = null
        handlerslots[listener.priority]!!.add(listener)
    }

    /**
     * Register a collection of new listeners in this handler list
     *
     * @param listeners listeners to register
     */
    fun registerAll(listeners: Collection<RegisteredListener>) {
        for (listener in listeners) {
            register(listener)
        }
    }

    /**
     * Bake HashMap and ArrayLists to 2d array - does nothing if not necessary
     */
    @Synchronized
    fun bake() {
        if (handlers != null) return  // don't re-bake when still valid
        val entries: MutableList<RegisteredListener> = ArrayList()
        for ((_, value) in handlerslots) {
            entries.addAll(value)
        }
        handlers = entries.toTypedArray()
    }

    val registeredListeners: Array<RegisteredListener>
        /**
         * Get the baked registered listeners associated with this handler list
         *
         * @return the array of registered listeners
         */
        get() {
            var handlers: Array<RegisteredListener>
            while (this.handlers.also { handlers = it!! } == null) bake() // This prevents fringe cases of returning null
            return handlers
        }

    companion object {
        /**
         * List of all HandlerLists which have been created, for use in bakeAll()
         */
        private val allLists = ArrayList<HandlerList>()
    }
}