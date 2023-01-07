package com.github.fernthedev.lightchat.core.packets.handshake

import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.PacketInfo

/**
 * The final packet sent in the handshake
 */
@PacketInfo(name = "CONNECTED_PACKET")
class ConnectedPacket(val name: String, val os: String, versionData: VersionData, langFramework: String) : Packet() {
    val versionData: VersionDataString
    val langFramework: String
    fun getVersionData(): VersionData {
        return VersionData(versionData.version, versionData.minVersion)
    }

    init {
        this.versionData = VersionDataString(versionData.version.toString(), versionData.minVersion.toString())
        this.langFramework = langFramework
    }

    override fun toString(): String {
        return "ConnectedPacket{" +
                "name='" + name + '\'' +
                ", os='" + os + '\'' +
                '}'
    }
}