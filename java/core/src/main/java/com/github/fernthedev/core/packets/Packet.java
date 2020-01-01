package com.github.fernthedev.core.packets;

import com.github.fernthedev.core.encryption.codecs.AcceptablePacketTypes;
import lombok.Getter;
import lombok.NonNull;

public abstract class Packet implements AcceptablePacketTypes {
    private static final long serialVersionUID = -5039841570298012421L;

    @Getter
    protected final transient @NonNull String packetName;

    public static String getPacketName(Class<?> packet) {
        if (!packet.isAnnotationPresent(PacketInfo.class)) throw new IllegalStateException("Packet must have a packet info annotation");

        return packet.getAnnotation(PacketInfo.class).name();
    }


    protected Packet() {
        if (!getClass().isAnnotationPresent(PacketInfo.class)) throw new IllegalStateException("Packet must have a packet info annotation");

        packetName = getClass().getAnnotation(PacketInfo.class).name();
    }
}
