package com.github.fernthedev.server.event.chat;

import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.event.Cancellable;
import com.github.fernthedev.server.event.Event;
import com.github.fernthedev.server.event.HandlerList;

public class ChatEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    private CommandSender sender;
    private String message;
    private boolean isCommand;

    public ChatEvent(CommandSender sender, String message,boolean isCommand) {
        this.sender = sender;
        this.message = message;
        this.isCommand = isCommand;
    }

    public ChatEvent(CommandSender sender, String message,boolean isCommand, boolean async) {
        super(async);
        this.sender = sender;
        this.message = message;
        this.isCommand = isCommand;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
