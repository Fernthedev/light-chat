package com.github.fernthedev.core.packets.handshake;

import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.encryption.util.RSAEncryptionUtil;
import com.github.fernthedev.core.packets.PacketInfo;

import java.security.PublicKey;

@PacketInfo(name = "INITIAL_HANDSHAKE_PACKET")
public class InitialHandshakePacket extends Packet {

    public InitialHandshakePacket(PublicKey publicKey) {
        this.publicKey = RSAEncryptionUtil.toBase64(publicKey);
    }

    private String publicKey;

    public PublicKey getPublicKey() {
        return RSAEncryptionUtil.toPublicKey(publicKey);
    }

}
