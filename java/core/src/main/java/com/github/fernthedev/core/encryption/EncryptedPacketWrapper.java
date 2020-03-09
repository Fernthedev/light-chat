package com.github.fernthedev.core.encryption;

import com.github.fernthedev.core.packets.Packet;

public class EncryptedPacketWrapper extends PacketWrapper<EncryptedBytes> {

    public EncryptedPacketWrapper(EncryptedBytes encryptedBytes, Packet packet, int packetId) {
        super(encryptedBytes, packet.getPacketName(), packetId);
        ENCRYPT = true;
    }


}
