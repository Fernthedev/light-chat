package com.github.fernthedev.lightchat.core.api

@FunctionalInterface
interface EventListener<T : Event> {
    fun invoke(event: T)
}

class EventHandler {

    private val map: MutableMap<Class<out Event>, MutableList<EventListener<out Event>>> = HashMap()

    fun <T : Event> add(clazz: Class<T>, listener: (event: T) -> Unit): EventListener<T> {
        val listener = object : EventListener<T> {
            override fun invoke(event: T) {
                listener(event)
            }
        }
        add(clazz, listener)
        return listener
    }

    fun <T : Event> add(clazz: Class<T>, listener: EventListener<T>) {
        val list = map[clazz]

        if (list == null) {
            map[clazz] = mutableListOf(listener)
        } else {
            list.add(listener)
        }
    }

    fun <T : Event> remove(clazz: Class<T>, listener: EventListener<T>) {
        map[clazz]?.remove(listener)
    }

    fun <T : Event> callEvent(event: T) {
        map[event.javaClass]?.forEach {
            @Suppress("UNCHECKED_CAST")
            (it as EventListener<T>).invoke(event)
        }
    }

}