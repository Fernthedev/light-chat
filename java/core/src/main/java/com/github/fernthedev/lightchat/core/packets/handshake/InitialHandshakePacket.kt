package com.github.fernthedev.lightchat.core.packets.handshake

import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil
import com.github.fernthedev.lightchat.core.packets.PacketInfo
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import java.security.PublicKey


@PacketInfo(name = "INITIAL_HANDSHAKE_PACKET")
class InitialHandshakePacket private constructor(
    private val publicKey: String,
    private val versionData: VersionDataString
) : PacketJSON() {



    constructor(publicKey: PublicKey, versionData: VersionData) : this(
        RSAEncryptionUtil.toBase64(publicKey),
        VersionDataString(versionData.version.toString(), versionData.minVersion.toString())
    )


    fun getVersionData(): VersionData {
        return VersionData(versionData.version, versionData.minVersion)
    }

    fun getPublicKey(): PublicKey? {
        return RSAEncryptionUtil.toPublicKey(publicKey)
    }
}