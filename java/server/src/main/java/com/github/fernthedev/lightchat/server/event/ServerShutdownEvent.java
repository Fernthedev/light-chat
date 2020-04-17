package com.github.fernthedev.lightchat.server.event;

import com.github.fernthedev.lightchat.core.api.event.api.Event;
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList;

public class ServerShutdownEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public void foo() {

    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
