package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.lightchat.core.packets.Packet;

public interface SenderInterface {
    void sendPacket(Packet packet);

    String getName();
}
