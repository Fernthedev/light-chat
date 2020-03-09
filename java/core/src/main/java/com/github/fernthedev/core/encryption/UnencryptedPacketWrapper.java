package com.github.fernthedev.core.encryption;

import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.PacketRegistry;
import com.github.fernthedev.core.encryption.codecs.AcceptablePacketTypes;
import com.google.gson.Gson;

/**
 * Wraps a packet not meant to be encrypted
 */
public class UnencryptedPacketWrapper extends PacketWrapper<AcceptablePacketTypes> implements AcceptablePacketTypes {

    private static final Gson gson = new Gson();

    protected UnencryptedPacketWrapper() {}

    public UnencryptedPacketWrapper(Packet jsonObject, int packetId) {
        super(jsonObject, jsonObject.getPacketName(), packetId);

        if (PacketRegistry.checkIfRegistered(jsonObject) == PacketRegistry.RegisteredReturnValues.NOT_IN_REGISTRY) {
            throw new IllegalArgumentException("The packet trying to be wrapped is not registered. \"" + jsonObject.getClass() + "\"");
        }

        ENCRYPT = false;
    }


}
