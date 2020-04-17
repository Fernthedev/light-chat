package com.github.fernthedev.lightchat.core.encryption;

import com.github.fernthedev.lightchat.core.packets.Packet;

public class EncryptedPacketWrapper extends PacketWrapper<EncryptedBytes> {

    public EncryptedPacketWrapper(EncryptedBytes encryptedBytes, Packet packet, int packetId) {
        super(encryptedBytes, packet.getPacketName(), packetId);
        ENCRYPT = t