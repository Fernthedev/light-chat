package com.github.fernthedev.lightchat.core.encryption;

import com.github.fernthedev.lightchat.core.codecs.JSONHandler;
import com.github.fernthedev.lightchat.core.packets.Packet;

public class EncryptedPacketWrapper extends PacketWrapper {

    public EncryptedPacketWrapper(EncryptedBytes encryptedBytes, JSONHandler handler, Packet packet, int packetId) {
        super(handler.toJson(encryptedBytes), packet.getPacketName(), packetId);
        ENCRYPT = true;
    }


}
