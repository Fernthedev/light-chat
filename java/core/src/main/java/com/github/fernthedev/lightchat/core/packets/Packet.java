package com.github.fernthedev.lightchat.core.packets;

import com.github.fernthedev.lightchat.core.encryption.codecs.AcceptablePacketTypes;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.Modifier;

@ToString
public abstract class Packet implements AcceptablePacketTypes {
    private static final long serialVersionUID = -5039841570298012421L;


    public String getPacketName() {
        if (packetName == null) packetName = getClass().getAnnotation(PacketInfo.class).name();
        return packetName;
    }

    protected transient @NonNull String packetName;

    public static String getPacketName(Class<? extends Packet> packet) {
        if (packet.equals(Packet.class)) throw new IllegalArgumentException("The class cannot be " + Packet.class.getName());

        if (!Packet.class.isAssignableFrom(packet)) throw new IllegalStateException("Packet " + packet.getName() + " must extend " + Packet.class.getName());

        if (Modifier.isAbstract(packet.getModifiers()) || packet.isInterface()) throw new IllegalArgumentException("The class cannot be abstract or interface.");

        if (!packet.isAnnotationPresent(PacketInfo.class)) throw new IllegalStateException("Packet " + packet.getName() + " must have a packet info annotation");

        return packet.getAnnotation(PacketInfo.class).name();
    }


    protected Packet() {
        if (!getClass().isAnnotationPresent(PacketInfo.class)) throw new IllegalStateException("Packet must have a packet info annotation");

        packetName = getClass().getAnnotation(PacketInfo.class)