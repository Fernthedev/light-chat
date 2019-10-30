package com.github.fernthedev.universal.encryption;

public class EncryptedPacketWrapper extends PacketWrapper<EncryptedBytes> {

    public EncryptedPacketWrapper(EncryptedBytes encryptedBytes, Class<?> aClass) {
        super(encryptedBytes, aClass);
        ENCRYPT = true;
    }


}
