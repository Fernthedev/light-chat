package com.github.fernthedev.packets.handshake;

import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.universal.encryption.util.RSAEncryptionUtil;

import java.security.PublicKey;

public class InitialHandshakePacket extends Packet {

    public InitialHandshakePacket(PublicKey publicKey) {
        this.publicKey = RSAEncryptionUtil.toBase64(publicKey);
    }

    private String publicKey;

    public PublicKey getPublicKey() {
        return RSAEncryptionUtil.toPublicKey(publicKey);
    }

}
