package com.github.fernthedev.server.api;

import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.server.ClientPlayer;

@FunctionalInterface
public interface IPacketHandler {

    void handlePacket(Packet packet, ClientPlayer clientPlayer);

}
