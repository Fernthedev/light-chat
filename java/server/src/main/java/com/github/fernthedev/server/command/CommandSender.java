package com.github.fernthedev.server.command;

import com.github.fernthedev.packets.Packet;

public interface CommandSender {


    void sendPacket(Packet packet);

    void sendMessage(String message);


    String getName();
}
