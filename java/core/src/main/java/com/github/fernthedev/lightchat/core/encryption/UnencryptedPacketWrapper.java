package com.github.fernthedev.lightchat.core.encryption;

import com.github.fernthedev.lightchat.core.PacketRegistry;
import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes;
import com.github.fernthedev.lightchat.core.packets.Packet;

/**
 * Wraps a packet not meant to be encrypted
 */
public class UnencryptedPacketWrapper extends PacketWrapper<AcceptablePacketTypes> implements AcceptablePacketTypes {
    protected UnencryptedPacketWrapper() {}

    public UnencryptedPacketWrapper(Packet jsonObject, int packetId) {
        super(jsonObject, jsonObject.getPacketName(), packetId);

        if (PacketRegistry.checkIfRegistered(jsonObject) == PacketRegistry.RegisteredReturnValues.NOT_IN_REGISTRY) {
            throw new IllegalArgumentException("The packet trying to be wrapped is not registered. \"" + jsonObject.getClass() + "\"");
        }

        ENCRYPT = false;
    }


}
