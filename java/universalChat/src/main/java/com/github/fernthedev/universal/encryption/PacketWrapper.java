package com.github.fernthedev.universal.encryption;

import lombok.Getter;

public class PacketWrapper<T> {



    protected boolean ENCRYPT = false;

    @Getter
    private T jsonObject;

    @Getter
    private String aClass;


    @Override
    public String toString() {
        return "PacketWrapper{" +
                ", aClass='" + aClass + '\'' +
                '}';
    }

    public PacketWrapper(T object, Class<?> aClass) {
        this.aClass = aClass.getName();
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
