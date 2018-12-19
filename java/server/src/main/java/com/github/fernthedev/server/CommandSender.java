package com.github.fernthedev.server;

import com.github.fernthedev.packets.Packet;

public interface CommandSender {


    void sendPacket(Packet packet);

    void sendMessage(String message);


}
