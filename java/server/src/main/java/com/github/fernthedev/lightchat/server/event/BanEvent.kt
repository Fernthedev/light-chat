package com.github.fernthedev.lightchat.server.event;

import com.github.fernthedev.lightchat.core.api.event.api.Cancellable;
import com.github.fernthedev.lightchat.core.api.event.api.Event;
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList;
import lombok.Getter;

/**
 * Called when client has successfully established a secure and valid connection
 */
public class BanEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    /**
     * If true, player was banned
     * if false, player was unbanned
     */
    @Getter
    private final boolean banned;

    @Getter
    private final String bannedIP;

    public BanEvent(boolean banned, String bannedIP) {
        this.banned = banned;
        this.bannedIP = bannedIP;
    }

    public BanEvent(boolean isAsync, boolean banned, String bannedIP) {
        super(isAsync);
        this.banned = banned;
        this.bannedIP = bannedIP;
    }

    /**
     * Gets the cancellation state of this com.github.fernthedev.client.event. A cancelled com.github.fernthedev.client.event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this com.github.fernthedev.client.event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this com.github.fernthedev.client.event. A cancelled com.github.fernthedev.client.event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this com.github.fernthedev.client.event
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
