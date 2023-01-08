package com.github.fernthedev.lightchat.server.api;

import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.server.ClientConnection;

@FunctionalInterface
public interface IPacketHandler {

    void handlePacket(Packet packet, ClientConnection clientConnection, int packetId);

}
