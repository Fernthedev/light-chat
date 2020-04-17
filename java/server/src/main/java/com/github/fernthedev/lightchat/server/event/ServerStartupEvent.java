package com.github.fernthedev.lightchat.server.event;

import com.github.fernthedev.lightchat.core.api.event.api.Event;
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerStartupEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    /**
     * This constructor is used to explicitly declare an event as synchronous
     * or asynchronous.
     *
     * @param isAsync true indicates the com.github.fernthedev.client.event will fire asynchronously, false
     *     by default from default constructor
     */
    public ServerStartupEvent(boolean isAsync) {
        super(isAsync);
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
