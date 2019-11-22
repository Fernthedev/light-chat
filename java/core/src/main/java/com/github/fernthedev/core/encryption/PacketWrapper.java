package com.github.fernthedev.core.encryption;

import lombok.Getter;

public class PacketWrapper<T> {



    protected boolean ENCRYPT = false;

    @Getter
    private T jsonObject;

    @Getter
    private String packetIdentifier;


    @Override
    public String toString() {
        return "PacketWrapper{" +
                ", aClass='" + packetIdentifier + '\'' +
                '}';
    }

    public PacketWrapper(T object, String packetIdentifier) {


        this.packetIdentifier = packetIdentifier;
        this.jsonObject = object;
    }

//    @Deprecated
//    public PacketWrapper(@NonNull Object object) {
//        this.jsonObject = gson.toJson(object);
//        this.aClass = object.getClass().getName();
//    }

    public boolean encrypt() {
        return ENCRYPT;
    }



    protected PacketWrapper() {}
}
