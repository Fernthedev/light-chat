package com.github.fernthedev.lightchat.client.api;

import com.github.fernthedev.lightchat.core.packets.Packet;

@FunctionalInterface
public interface IPacketHandler {

    void handlePacket(Packet packet, int packetId);

}
