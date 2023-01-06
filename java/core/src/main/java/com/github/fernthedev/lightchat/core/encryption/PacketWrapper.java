package com.github.fernthedev.lightchat.core.encryption;

import lombok.Getter;

import java.io.Serializable;

public class PacketWrapper implements Serializable {

    protected boolean ENCRYPT = false;

    @Getter
    private String jsonObject;

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

    public PacketWrapper(String object, String packetIdentifier, int packetId) {
        this.packetIdentifier = packetIdentifier;
        this.jsonObject = object;
        this.packetId = packetId;
    }

    public boolean encrypt() {
        return ENCRYPT;
    }



    protected PacketWrapper() {}
}
