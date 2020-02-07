package com.github.fernthedev.client.api;

import com.github.fernthedev.core.packets.Packet;

@FunctionalInterface
public interface IPacketHandler {

    void handlePacket(Packet packet);

}
