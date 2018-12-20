package com.github.fernthedev.light;

import com.github.fernthedev.server.CommandSender;

public class PlayerInfo {

    public final CommandSender sender;

    public boolean authenticated = false;

    public ChangePassword.Mode mode = ChangePassword.Mode.AUTHENTICATE;
    public int tries = 0;



    public PlayerInfo(CommandSender sender) {
        this.sender = sender;
    }



}
