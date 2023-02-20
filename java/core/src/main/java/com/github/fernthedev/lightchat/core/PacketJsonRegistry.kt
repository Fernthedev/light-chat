package com.github.fernthedev.lightchat.core

import com.github.fernthedev.lightchat.core.exceptions.PacketNotInRegistryException
import com.github.fernthedev.lightchat.core.exceptions.PacketRegistryException
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket
import com.github.fernthedev.lightchat.core.packets.latency.LatencyPacket
import org.reflections.Reflections
import java.util.concurrent.ConcurrentHashMap

object PacketJsonRegistry {
    private val PACKET_JSON_REGISTRY = ConcurrentHashMap<String, Class<out PacketJSON>>()


    fun getPacketClassFromRegistry(name: String): Class<out PacketJSON> {
        if (!PACKET_JSON_REGISTRY.containsKey(name)) throw PacketNotInRegistryException("The packet registry does not contain packet \"$name\" in the registry. Make sure it is spelled correctly and is case-sensitive.")
        return PACKET_JSON_REGISTRY[name]!!
    }

    fun registerPacket(packetJSON: PacketJSON): Class<out PacketJSON> {
        if (PACKET_JSON_REGISTRY.containsKey(packetJSON.packetName) && PACKET_JSON_REGISTRY[packetJSON.packetName] != packetJSON.javaClass) throw PacketRegistryException(
            "The packet " + packetJSON.javaClass.name + " tried to use packet name \"" + packetJSON.packetName + "\" which is already taken by the packet " + getPacketClassFromRegistry(
                packetJSON.packetName
            )
        )
        PACKET_JSON_REGISTRY[packetJSON.packetName] = packetJSON.javaClass
        return packetJSON.javaClass
    }

    fun <T : PacketJSON> registerPacket(packet: Class<T>): Class<T> {
        val name: String = PacketJSON.getPacketName(packet)
        if (PACKET_JSON_REGISTRY.containsKey(name) && PACKET_JSON_REGISTRY[name] != packet) throw PacketRegistryException(
            "The packet name \"$name\" is already taken by the packet " + getPacketClassFromRegistry(
                name
            )
        )
        PACKET_JSON_REGISTRY[name] = packet
        return packet
    }

    fun checkIfRegistered(packetJSON: PacketJSON): RegisteredReturnValues {
        if (!PACKET_JSON_REGISTRY.containsKey(packetJSON.packetName)) return RegisteredReturnValues.NOT_IN_REGISTRY
        return if (PACKET_JSON_REGISTRY[packetJSON.packetName] == packetJSON.javaClass) {
            RegisteredReturnValues.IN_REGISTRY
        } else {
            RegisteredReturnValues.IN_REGISTRY_DIFFERENT_PACKET
        }
    }

    fun registerDefaultPackets() {
        // Force load these classes
        // since Kotlin does some weird stuff that doesn't eagerly load them?
        PacketJSON::class.java.packageName
        LatencyPacket::class.java.packageName
        ConnectedPacket::class.java.packageName

        for (packageT in Package.getPackages()
            .filter { aPackage: Package -> aPackage.name.startsWith(StaticHandler.PACKET_JSON_PACKAGE) }
            .toList()) {
            StaticHandler.core.logger.debug("Registering the package {}", packageT.name)
            registerPacketPackage(packageT.name)
        }
    }

    /**
     * Preferably the best choice since it guarantees it will use the package name
     * from the class rather than manually typing it in.
     * It's to avoid human errors in code
     * @param packetJSON
     */
    fun registerPacketPackageFromPacket(packetJSON: PacketJSON) {
        registerPacketPackageFromClass(packetJSON.javaClass)
    }

    /**
     * Preferably the best choice since it guarantees it will use the package name
     * from the class rather than manually typing it in.
     * It's to avoid human errors in code
     * @param packetJSON
     */
    @JvmStatic
    fun registerPacketPackageFromClass(packetJSON: Class<out PacketJSON>) {
        registerPacketPackage(packetJSON.getPackage().name)
    }

    fun registerPacketPackage(packageName: String) {
        val classes = Reflections(packageName).getSubTypesOf(
            PacketJSON::class.java
        )
        for (packetClass in classes) {
            StaticHandler.core.logger.debug("Registering the class {}", packetClass)
            try {
                registerPacket(packetClass)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    enum class RegisteredReturnValues {
        NOT_IN_REGISTRY, IN_REGISTRY, IN_REGISTRY_DIFFERENT_PACKET
    }
}