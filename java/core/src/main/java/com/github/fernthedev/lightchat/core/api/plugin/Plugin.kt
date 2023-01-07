package com.github.fernthedev.lightchat.core.api.plugin

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.api.event.api.*
import org.apache.commons.lang3.Validate
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

abstract class Plugin {
    fun createRegisteredListeners(
        listener: Listener,
        plugin: Plugin
    ): Map<Class<out Event>, MutableSet<RegisteredListener>> {
        Validate.notNull(plugin, "Plugin can not be null")
        Validate.notNull(listener, "Listener can not be null")
        val ret: MutableMap<Class<out Event>, MutableSet<RegisteredListener>> = HashMap()
        val methods: Set<Method>
        try {
            val publicMethods = listener.javaClass.methods
            val privateMethods = listener.javaClass.declaredMethods
            methods = HashSet(publicMethods.size + privateMethods.size, 1.0f)
            Collections.addAll(methods, *publicMethods)
            Collections.addAll(methods, *privateMethods)
        } catch (e: NoClassDefFoundError) {
            StaticHandler.core.logger.error("Plugin " + " has failed to register events for " + listener.javaClass + " because " + e.message + " does not exist.")
            return ret
        }
        for (method in methods) {
            val eh = method.getAnnotation(
                EventHandler::class.java
            ) ?: continue
            // Do not register bridge or synthetic methods to avoid com.github.fernthedev.client.event duplication
            // Fixes SPIGOT-893
            if (method.isBridge || method.isSynthetic) {
                continue
            }
            lateinit var checkClass: Class<*>
            if (method.parameterTypes.size != 1 || !Event::class.java.isAssignableFrom(
                    method.parameterTypes[0].also { checkClass = it })
            ) {
                StaticHandler.core.logger.info(" attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.javaClass)
                continue
            }
            val eventClass = checkClass.asSubclass(
                Event::class.java
            )
            method.isAccessible = true
            var eventSet = ret[eventClass]
            if (eventSet == null) {
                eventSet = HashSet()
                ret[eventClass] = eventSet
            }
            val executor = object : EventExecutor {
                override fun execute(listener: Listener, event: Event) {
                    try {
                        if (!eventClass.isAssignableFrom(event.javaClass)) {
                                return
                            }
                        // Spigot start
                        method.invoke(listener, event)
                        // Spigot end
                    } catch (ex: InvocationTargetException) {
                        throw EventException(ex.cause!!)
                    } catch (t: Throwable) {
                        throw EventException(t)
                    }
                }
            }
            eventSet.add(RegisteredListener(listener, executor, plugin, eh.priority, eh.ignoreCancelled))
        }
        return ret
    }
}