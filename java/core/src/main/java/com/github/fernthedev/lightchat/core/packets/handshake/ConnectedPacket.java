package com.github.fernthedev.lightchat.core.packets.handshake;

import com.github.fernthedev.lightchat.core.VersionData;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.PacketInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Final packet sent in the handshake
 */
@Getter
@PacketInfo(name = "CONNECTED_PACKET")
public class ConnectedPacket extends Packet {
    @NonNull
    private String name;

    @Getter(AccessLevel.NONE)
    @NonNull
    private String os;

    @NonNull
    private VersionDataString versionData;

    public VersionData getVersionData() {
        return new VersionData(versionData.getVersion(), versionData.getMinVersion());
    }



//    @NonNull
//    private UUID uuid;

    public ConnectedPacket(@NonNull String name, @NonNull String os, VersionData versionData) { //, @NonNull UUID uuid) {
        this.name = name;
        this.os = os;
        this.versionData = new VersionDataString(versionData.getVersion().toString(), versionData.getMinVersion().toString());
//        this.uuid = uuid;
    }


    @Override
    public String toString() {
        return "ConnectedPacket{" +
                "name='" + name + '\'' +
                ", os='" + os + '\'' +
                '}';
    }


    public String getOS() {
        return os;
    }
}
