package com.github.fernthedev.lightchat.core.api.event.api;

/**
 * Interface which defines the class for com.github.fernthedev.client.event call backs to plugins
 */
public interface EventExecutor {
    public void execute(Listener listener, Event event) throws EventException;
}
