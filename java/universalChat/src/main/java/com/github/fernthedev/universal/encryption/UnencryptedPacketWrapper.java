package com.github.fernthedev.universal.encryption;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Wraps a packet not meant to be encrypted
 */
public class UnencryptedPacketWrapper extends PacketWrapper<String> implements Serializable {

    private static final Gson gson = new Gson();

    protected UnencryptedPacketWrapper() {}

    public UnencryptedPacketWrapper(String jsonObject, Class<?> aClass) {
        super(jsonObject, aClass);
        ENCRYPT = false;
    }

    public UnencryptedPacketWrapper(Object jsonObject) {
        super(gson.toJson(jsonObject), jsonObject.getClass());
        ENCRYPT = false;
    }


}
