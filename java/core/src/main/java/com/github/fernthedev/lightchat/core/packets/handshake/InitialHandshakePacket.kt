package com.github.fernthedev.lightchat.core.packets.handshake

import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.PacketInfo
import java.security.PublicKey

@PacketInfo(name = "INITIAL_HANDSHAKE_PACKET")
class InitialHandshakePacket(publicKey: PublicKey, versionData: VersionData) : Packet() {
    private val publicKey: String?
    private val versionData: VersionDataString

    init {
        this.publicKey = RSAEncryptionUtil.toBase64(publicKey)
        this.versionData = VersionDataString(versionData.version.toString(), versionData.minVersion.toString())
    }

    fun getVersionData(): VersionData {
        return VersionData(versionData.version, versionData.minVersion)
    }

    fun getPublicKey(): PublicKey? {
        return RSAEncryptionUtil.toPublicKey(publicKey)
    }
}