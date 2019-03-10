package com.github.fernthedev.packets;

import lombok.*;

import java.util.UUID;

@Getter
public class ConnectedPacket extends Packet {
    @NonNull
    private String name;

    @Getter(AccessLevel.NONE)
    @NonNull
    private String os;

    @NonNull
    private UUID uuid;

    public ConnectedPacket(@NonNull String name, @NonNull String os, @NonNull UUID uuid) {
        this.name = name;
        this.os = os;
        this.uuid = uuid;
    }

    @Setter
    @Getter
    @NonNull
    private String privateKey;

    public String getOS() {
        return os;
    }
}
