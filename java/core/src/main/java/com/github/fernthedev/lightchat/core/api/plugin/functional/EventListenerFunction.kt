package com.github.fernthedev.lightchat.core.api.plugin.functional;

import com.github.fernthedev.lightchat.core.api.event.api.Event;
import com.github.fernthedev.lightchat.core.api.event.api.EventHandler;
import com.github.fernthedev.lightchat.core.api.event.api.Listener;

/**
 * Use for functional programming
 * @param <T> The event
 */
@FunctionalInterface
public interface EventListenerFunction<T extends Event> extends Listener {

    @EventHandler
    void onEvent(T event);

}
