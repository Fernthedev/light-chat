package com.github.fernthedev.lightchat.server.event;

import com.github.fernthedev.lightchat.core.api.event.api.HandlerList;
import com.github.fernthedev.lightchat.server.security.AuthenticationManager;

/**
 * Called when client has successfully established a secure and valid connection
 */
public class AuthenticateRequestEvent extends AuthenticateEvent {


    private static final HandlerList handlers = new HandlerList();

    public AuthenticateRequestEvent(AuthenticationManager.PlayerInfo playerInfo) {
        super(playerInfo);
    }

    public AuthenticateRequestEvent(AuthenticationManager.PlayerInfo playerInfo, boolean async) {
        super(playerInfo, async);
    }


    public static HandlerList getHandlerList() {
        return handlers;
    }
}
