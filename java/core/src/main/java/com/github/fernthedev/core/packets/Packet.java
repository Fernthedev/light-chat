package com.github.fernthedev.core.packets;

import com.github.fernthedev.core.encryption.codecs.AcceptablePacketTypes;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Modifier;

public abstract class Packet implements AcceptablePacketTypes {
    private static final long serialVersionUID = -5039841570298012421L;

    @Getter
    protected final transient @NonNull String packetName;

    public static String getPacketName(Class<?> packet) {
        if (packet.equals(Packet.class)) throw new IllegalArgumentException("The class cannot be " + Packet.class.getName());

        if (!Packet.class.isAssignableFrom(packet)) throw new IllegalStateException("Packet " + packet.getName() + " must extend " + Packet.class.getName());

        if (Modifier.isAbstract(packet.getModifiers()) || packet.isInterface()) throw new IllegalArgumentException("The class cannot be abstract or interface.");

        if (!packet.isAnnotationPresent(PacketInfo.class)) throw new IllegalStateException("Packet " + packet.getName() + " must have a packet info annotation");

        return packet.getAnnotation(PacketInfo.class).name();
    }


    protected Packet() {
        if (!getClass().isAnnotationPresent(PacketInfo.class)) throw new IllegalStateException("Packet must have a packet info annotation");

        packetName = getClass().getAnnotation(PacketInfo.class).name();
    }
}
