package com.github.fernthedev.core;

import com.github.fernthedev.core.packets.Packet;
import org.reflections.Reflections;

import java.util.*;
import java.util.stream.Collectors;

public class PacketRegistry {

    private PacketRegistry() {}

    private static final Map<String, Class<? extends Packet>> PACKET_REGISTRY = Collections.synchronizedMap(new HashMap<>());

    public static Class<? extends Packet> getPacketClassFromRegistry(String name) {
        if (!PACKET_REGISTRY.containsKey(name)) throw new IllegalArgumentException("The packet registry does not contain packet \"" + name + "\" in the registry. Make sure it is spelled correctly and is case-sensitive.");

        return PACKET_REGISTRY.get(name);
    }

    public static Class<? extends Packet> registerPacket(Packet packet) {
        if(PACKET_REGISTRY.containsKey(packet.getPacketName()) && PACKET_REGISTRY.get(packet.getPacketName()) != packet.getClass()) throw new IllegalArgumentException("The packet " + packet.getClass().getName() + " tried to use packet name \"" + packet.getPacketName() + "\" which is already taken by the packet " + getPacketClassFromRegistry(packet.getPacketName()));

        PACKET_REGISTRY.put(packet.getPacketName(), packet.getClass());

        return packet.getClass();
    }

    public static <T extends Packet> Class<T> registerPacket(Class<T> packet) {
        String name = Packet.getPacketName(packet);

        if(PACKET_REGISTRY.containsKey(name) && PACKET_REGISTRY.get(name) != packet) throw new IllegalArgumentException("The packet name \"" + name + "\" is already taken by the packet " + getPacketClassFromRegistry(name));

        PACKET_REGISTRY.put(name, packet);

        return packet;
    }

    public static RegisteredReturnValues checkIfRegistered(Packet packet) {
        if (!PACKET_REGISTRY.containsKey(packet.getPacketName())) return RegisteredReturnValues.NOT_IN_REGISTRY;

        return PACKET_REGISTRY.get(packet.getPacketName()) == packet.getClass() ? RegisteredReturnValues.IN_REGISTRY : RegisteredReturnValues.IN_REGISTRY_DIFFERENT_PACKET;
    }

    public static void registerDefaultPackets() {

        for (Package packageT : Arrays.stream(Package.getPackages())
                .parallel()
                .filter(aPackage -> aPackage.getName().startsWith(StaticHandler.PACKET_PACKAGE))
                .collect(Collectors.toList())) {

            StaticHandler.getCore().getLogger().debug("Registering the package {}", packageT.getName());
            registerPacketPackage(packageT.getName());
        }
    }

    public static void registerPacketPackage(String packageName) {
        Set<Class<? extends Packet>> classes = new Reflections(packageName).getSubTypesOf(Packet.class);

        for (Class<? extends Packet> packetClass : classes) {
            StaticHandler.getCore().getLogger().debug("Registering the class {}", packetClass);

            try {
                registerPacket(packetClass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public enum RegisteredReturnValues {
        NOT_IN_REGISTRY,
        IN_REGISTRY,
        IN_REGISTRY_DIFFERENT_PACKET
    }
}
