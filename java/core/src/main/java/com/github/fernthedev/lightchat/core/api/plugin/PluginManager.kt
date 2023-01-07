package com.github.fernthedev.lightchat.core.api.plugin

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.api.event.api.*
import com.github.fernthedev.lightchat.core.api.plugin.exception.IllegalPluginAccessException
import org.apache.commons.lang3.Validate

class PluginManager {
    private val listeners: MutableList<Listener> = ArrayList()

    /**
     * Calls an event with the given details.
     *
     *
     * This method only synchronizes when the event is not asynchronous.
     *
     * @param event Event details
     */
    fun callEvent(event: Event) {
        if (event.isAsynchronous) {
            check(!Thread.holdsLock(this)) { event.eventName + " cannot be triggered asynchronously from inside synchronized code." }
            fireEvent(event)
        } else {
            synchronized(this) { fireEvent(event) }
        }
    }

    private fun fireEvent(event: Event) {
        val handlers = event.handlers
        val listeners = handlers.registeredListeners
        for (registration in listeners) {
            try {
                registration.callEvent(event)
            } catch (ex: Exception) {
                StaticHandler.core.logger.error("Could not pass event " + event.eventName + " to ", ex)
            }
        }
    }

    /**
     * Registers the given com.github.fernthedev.client.event to the specified listener using a directly
     * passed EventExecutor
     *
     * @param event Event class to register
     * @param listener PlayerListener to register
     * @param priority Priority of this com.github.fernthedev.client.event
     * @param executor EventExecutor to register
     * @param plugin Plugin to register
     * @param ignoreCancelled Do not call executor if com.github.fernthedev.client.event was already
     * cancelled
     */
    @JvmOverloads
    fun registerEvent(
        event: Class<out Event?>?,
        listener: Listener,
        priority: EventPriority,
        executor: EventExecutor,
        plugin: Plugin,
        ignoreCancelled: Boolean = false
    ) {
        Validate.notNull(listener, "Listener cannot be null")
        Validate.notNull(priority, "Priority cannot be null")
        Validate.notNull(executor, "Executor cannot be null")
        Validate.notNull(plugin, "Plugin cannot be null")
        getEventListeners(event).register(RegisteredListener(listener, executor, plugin, priority, ignoreCancelled))
    }

    fun registerEvents(listener: Listener) {
        registerEvents(listener, CORE_PLUGIN)
    }

    /**
     * @param listener
     * @param plugin
     */
    @Deprecated(
        """Use {@link #registerEvents(Listener)} which defaults to {@link #CORE_PLUGIN}
      """
    )
    fun registerEvents(listener: Listener, plugin: Plugin) {
        for ((key, value) in plugin.createRegisteredListeners(listener, plugin)) {
            getEventListeners(getRegistrationClass(key)).registerAll(value)
        }
    }

    private fun getEventListeners(type: Class<out Event?>?): HandlerList {
        return try {
            val method = getRegistrationClass(type)!!.getDeclaredMethod("getHandlerList")
            method.isAccessible = true
            method.invoke(null) as HandlerList
        } catch (e: Exception) {
            throw IllegalPluginAccessException(e.toString())
        }
    }

    private fun getRegistrationClass(clazz: Class<out Event?>?): Class<out Event?>? {
        return try {
            clazz!!.getDeclaredMethod("getHandlerList")
            clazz
        } catch (e: NoSuchMethodException) {
            if (clazz!!.superclass != null && clazz.superclass != Event::class.java
                && Event::class.java.isAssignableFrom(clazz.superclass)
            ) {
                getRegistrationClass(
                    clazz.superclass.asSubclass(
                        Event::class.java
                    )
                )
            } else {
                throw IllegalPluginAccessException("Unable to find handler list for event " + clazz.name + ". Static getHandlerList method required!")
            }
        }
    }

    fun registerListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    companion object {
        private val CORE_PLUGIN = CorePlugin()
    }
}