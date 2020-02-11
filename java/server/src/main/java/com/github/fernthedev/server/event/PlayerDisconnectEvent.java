package com.github.fernthedev.server.event;

import com.github.fernthedev.server.SenderInterface;
import com.github.fernthedev.server.event.api.Cancellable;
import com.github.fernthedev.server.event.api.Event;
import com.github.fernthedev.server.event.api.HandlerList;

/**
 * Called when client has successfully established a secure and valid connection
 */
public class PlayerDisconnectEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    private SenderInterface sender;

    public PlayerDisconnectEvent(SenderInterface sender) {
        this.sender = sender;
    }

    public PlayerDisconnectEvent(SenderInterface sender, boolean async) {
        super(async);
        this.sender = sender;
    }

    public SenderInterface getSender() {
        return sender;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
