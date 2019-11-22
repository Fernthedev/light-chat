package com.github.fernthedev.server.command;

import com.github.fernthedev.core.packets.Packet;

public interface CommandSender {


    void sendPacket(Packet packet);

    void sendMessage(String message);


    String getName();
}
