package com.github.fernthedev.lightchat.core.packets.handshake

import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.lightchat.core.packets.PacketInfo
import com.github.fernthedev.lightchat.core.packets.PacketJSON

/**
 * The final packet sent in the handshake
 */
@PacketInfo(name = "CONNECTED_PACKET")
class ConnectedPacket private constructor(
    val name: String, val os: String, val versionData: VersionDataString,
    val langFramework: String
) : PacketJSON() {

    fun getVersionData(): VersionData {
        return VersionData(versionData.version, versionData.minVersion)
    }

    constructor(name: String, os: String, versionData: VersionData, langFramework: String) : this(
        name,
        os,
        VersionDataString(versionData.version.toString(), versionData.minVersion.toString()),
        langFramework
    )

    override fun toString(): String {
        return "ConnectedPacket{" +
                "name='" + name + '\'' +
                ", os='" + os + '\'' +
                '}'
    }
}