package com.github.fernthedev.light;

import com.github.fernthedev.server.command.CommandSender;

public class PlayerInfo {

    public final CommandSender sender;

    public boolean authenticated = false;

    public AuthenticationManager.Mode mode = AuthenticationManager.Mode.AUTHENTICATE;
    public int tries = 0;



    public PlayerInfo(CommandSender sender) {
        this.sender = sender;
    }



}
