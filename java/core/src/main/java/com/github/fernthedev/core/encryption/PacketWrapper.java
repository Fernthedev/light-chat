package com.github.fernthedev.core.encryption;

import com.google.gson.Gson;
import lombok.Getter;

import java.io.Serializable;

public class PacketWrapper<T> implements Serializable {

    protected static Gson gson = new Gson();

    protected boolean ENCRYPT = false;

    @Getter
    private String jsonObject;

    @Getter
    private transient T jsonObjectInstance;

    @Getter
    private String packetIdentifier;

    /**
     * For packet order
     */
    @Getter
    private int packetId;


    @Override
    public String toString() {
        return "PacketWrapper{" +
                ", aClass='" + packetIdentifier + '\'' +
                '}';
    }

    public PacketWrapper(T object, String packetIdentifier, int packetId) {
        this.packetIdentifier = packetIdentifier;
        this.jsonObjectInstance = object;
        this.jsonObject = gson.toJson(object);
        this.packetId = packetId;
    }

    public boolean encrypt() {
        return ENCRYPT;
    }



    protected PacketWrapper() {}
}
