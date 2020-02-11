package com.github.fernthedev.server;

import com.github.fernthedev.core.packets.Packet;

public interface SenderInterface {
    void sendPacket(Packet packet);

    String getName();
}
