package com.github.fernthedev.lightchat.core.packets.handshake;

import com.github.fernthedev.lightchat.core.VersionData;
import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.PacketInfo;

import java.security.PublicKey;

@PacketInfo(name = "INITIAL_HANDSHAKE_PACKET")
public class InitialHandshakePacket extends Packet {

    public InitialHandshakePacket(PublicKey publicKey, VersionData versionData) {
        this.publicKey = RSAEncryptionUtil.toBase64(publicKey);
        this.versionData = new VersionDataString(versionData.getVersion().toString(), versionData.getMinVersion().toString());
    }

    private String publicKey;

    private VersionDataString versionData;

    public VersionData getVersionData() {
        return new VersionData(versionData.getVersion(), versionData.getMinVersion());
    }

    public PublicKey getPublicKey() {
        return RSAEncryptionUtil.toPublicKey(publicKey);
    }

}
