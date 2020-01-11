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


    @Override
    public String toString() {
        return "PacketWrapper{" +
                ", aClass='" + packetIdentifier + '\'' +
                '}';
    }

    public PacketWrapper(T object, String packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
        this.jsonObjectInstance = object;
        this.jsonObject = gson.toJson(object);
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
