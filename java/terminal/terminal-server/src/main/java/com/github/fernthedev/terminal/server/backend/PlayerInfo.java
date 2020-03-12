package com.github.fernthedev.terminal.server.backend;

import com.github.fernthedev.server.SenderInterface;

public class PlayerInfo {

    public final SenderInterface sender;

    public boolean authenticated = false;

    public AuthenticationManager.Mode mode = AuthenticationManager.Mode.AUTHENTICATE;
    public int tries = 0;



    public PlayerInfo(SenderInterface sender) {
        this.sender = sender;
    }



}