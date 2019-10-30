package com.github.fernthedev.packets.handshake;

import com.github.fernthedev.packets.Packet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Final packet sent in the handshake
 */
@Getter
public class ConnectedPacket extends Packet {
    @NonNull
    private String name;

    @Getter(AccessLevel.NONE)
    @NonNull
    private String os;



//    @NonNull
//    private UUID uuid;

    public ConnectedPacket(@NonNull String name, @NonNull String os) { //, @NonNull UUID uuid) {
        this.name = name;
        this.os = os;
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
