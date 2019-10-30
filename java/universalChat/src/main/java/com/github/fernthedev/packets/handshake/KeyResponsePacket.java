package com.github.fernthedev.packets.handshake;

import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.universal.encryption.util.RSAEncryptionUtil;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyResponsePacket extends Packet {

    public KeyResponsePacket(SecretKey secretKey, PublicKey publicKey) throws InvalidKeyException {
        this.secretKeyEncrypted = RSAEncryptionUtil.encryptKey(secretKey, publicKey);
    }
    
    private byte[] secretKeyEncrypted;

    public SecretKey getSecretKey(PrivateKey privateKey) throws InvalidKeyException {
        return RSAEncryptionUtil.decryptKey(secretKeyEncrypted, privateKey);
    }
    
}
