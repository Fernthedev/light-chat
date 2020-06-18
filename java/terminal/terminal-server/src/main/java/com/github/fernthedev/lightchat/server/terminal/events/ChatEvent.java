package com.github.fernthedev.lightchat.server.terminal.events;

import com.github.fernthedev.lightchat.core.api.event.api.Cancellable;
import com.github.fernthedev.lightchat.core.api.event.api.Event;
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList;
import com.github.fernthedev.lightchat.server.SenderInterface;

public class ChatEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    private SenderInterface sender;
    private String message;
    private boolean isCommand;

    public ChatEvent(SenderInterface sender, String message,boolean isCommand) {
        this.sender = sender;
        this.message = message;
        this.isCommand = isCommand;
    }

    public ChatEvent(SenderInterface sender, String message,boolean isCommand, boolean async) {
        super(async);
        this.sender = sender;
        this.message = message;
        this.isCommand = isCommand;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public SenderInterface getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
