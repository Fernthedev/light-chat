package com.github.fernthedev.server;

import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.server.command.CommandSender;

import java.io.Serializable;

public class Console implements CommandSender, Serializable {


    private static final long serialVersionUID = -7832219582908962549L;

    @Override
    public void sendPacket(Packet packet) {

    }

    @Override
    public void sendMessage(String message) {
        Server.getLogger().info(message);
    }

    @Override
    public String getName() {
        return "Server";
    }
}
