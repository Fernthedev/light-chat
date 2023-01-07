package com.github.fernthedev.lightchat.server.event;

import com.github.fernthedev.lightchat.core.api.event.api.Cancellable;
import com.github.fernthedev.lightchat.core.api.event.api.HandlerList;
import com.github.fernthedev.lightchat.server.security.AuthenticationManager;
import lombok.Getter;
import lombok.Setter;

/**
 * Called when client has successfully established a secure and valid connection
 */
public class AuthenticationAttemptedEvent extends AuthenticateEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Setter
    @Getter
    private EventStatus eventStatus;

    public AuthenticationAttemptedEvent(AuthenticationManager.PlayerInfo playerInfo, EventStatus eventStatus) {
        super(playerInfo);
        this.eventStatus = eventStatus;
    }

    public AuthenticationAttemptedEvent(AuthenticationManager.PlayerInfo playerInfo, boolean async, EventStatus eventStatus) {
        super(playerInfo, async);
        this.eventStatus = eventStatus;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum EventStatus {
        SUCCESS,
        ATTEMPT_FAILED,
        NO_MORE_TRIES
    }
}
