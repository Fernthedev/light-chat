package com.github.fernthedev.lightchat.core.packets.handshake;

import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil;
import com.github.fernthedev.lightchat.core.packets.PacketInfo;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

@PacketInfo(name = "KEY_RESPONSE_PACKET")
public class KeyResponsePacket extends Packet {

    public KeyResponsePacket(SecretKey secretKey, PublicKey publicKey) throws InvalidKeyException {
        this.secretKeyEncrypted = RSAEncryptionUtil.encryptKey(secretKey, publicKey);
    }
    
    private final byte[] secretKeyEncrypted;

    public SecretKey getSecretKey(PrivateKey privateKey) throws InvalidKeyException {
        return RSAEncryptionUtil.decryptKey(secretKeyEncrypted, privateKey);
    }
    
}
