package com.github.fernthedev.lightchat.core.api

@FunctionalInterface
fun interface EventListener<T : Event> {
    fun invoke(event: T)
}

class EventHandler {

    private val map: MutableMap<Class<out Event>, MutableList<EventListener<out Event>>> = HashMap()

    inline fun <reified T : Event> add(crossinline listener: (event: T) -> Unit): EventListener<T> {
        val wrapper = EventListener<T>{
            listener(it)
        }
        add(wrapper)
        return wrapper
    }

    fun <T : Event> add(clazz: Class<T>, listener: EventListener<T>) {
        val list = map[clazz]

        if (list == null) {
            map[clazz] = mutableListOf(listener)
        } else {
            list.add(listener)
        }
    }

    inline fun <reified T: Event> add(listener: EventListener<T>) {
        add(T::class.java, listener)
    }

    fun <T : Event> remove(clazz: Class<T>, listener: EventListener<T>) {
        map[clazz]?.remove(listener)
    }

    inline fun <reified T: Event> remove(listener: EventListener<T>) {
        remove(T::class.java, listener)
    }

    fun <T : Event> callEvent(event: T) {
        map[event.javaClass]?.forEach {
            @Suppress("UNCHECKED_CAST")
            (it as EventListener<T>).invoke(event)
        }
    }

}