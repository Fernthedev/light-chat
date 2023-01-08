package com.github.fernthedev.lightchat.core

import com.github.fernthedev.lightchat.core.exceptions.PacketNotInRegistryException
import com.github.fernthedev.lightchat.core.exceptions.PacketRegistryException
import com.github.fernthedev.lightchat.core.packets.Packet
import org.reflections.Reflections
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.toList

object PacketRegistry {
    private val PACKET_REGISTRY = ConcurrentHashMap<String, Class<out Packet>>()
    val packetRegistryCopy: Map<String, Class<out Packet>>
        get() = HashMap(PACKET_REGISTRY)

    fun getPacketClassFromRegistry(name: String): Class<out Packet> {
        if (!PACKET_REGISTRY.containsKey(name)) throw PacketNotInRegistryException("The packet registry does not contain packet \"$name\" in the registry. Make sure it is spelled correctly and is case-sensitive.")
        return PACKET_REGISTRY[name]!!
    }

    fun registerPacket(packet: Packet): Class<out Packet> {
        if (PACKET_REGISTRY.containsKey(packet.packetName) && PACKET_REGISTRY[packet.packetName] != packet.javaClass) throw PacketRegistryException(
            "The packet " + packet.javaClass.name + " tried to use packet name \"" + packet.packetName + "\" which is already taken by the packet " + getPacketClassFromRegistry(
                packet.packetName
            )
        )
        PACKET_REGISTRY[packet.packetName] = packet.javaClass
        return packet.javaClass
    }

    fun <T : Packet> registerPacket(packet: Class<T>): Class<T> {
        val name: String = Packet.getPacketName(packet)
        if (PACKET_REGISTRY.containsKey(name) && PACKET_REGISTRY[name] != packet) throw PacketRegistryException(
            "The packet name \"$name\" is already taken by the packet " + getPacketClassFromRegistry(
                name
            )
        )
        PACKET_REGISTRY[name] = packet
        return packet
    }

    fun checkIfRegistered(packet: Packet): RegisteredReturnValues {
        if (!PACKET_REGISTRY.containsKey(packet.packetName)) return RegisteredReturnValues.NOT_IN_REGISTRY
        return if (PACKET_REGISTRY[packet.packetName] == packet.javaClass) RegisteredReturnValues.IN_REGISTRY else RegisteredReturnValues.IN_REGISTRY_DIFFERENT_PACKET
    }

    fun registerDefaultPackets() {
        for (packageT in Arrays.stream(Package.getPackages())
            .parallel()
            .filter { aPackage: Package -> aPackage.name.startsWith(StaticHandler.PACKET_PACKAGE) }
            .toList()) {
            StaticHandler.core.logger.debug("Registering the package {}", packageT.name)
            registerPacketPackage(packageT.name)
        }
    }

    /**
     * Preferably the best choice since it guarantees it will use the package name
     * from the class rather than manually typing it in.
     * It's to avoid human errors in code
     * @param packet
     */
    fun registerPacketPackageFromPacket(packet: Packet) {
        registerPacketPackageFromClass(packet.javaClass)
    }

    /**
     * Preferably the best choice since it guarantees it will use the package name
     * from the class rather than manually typing it in.
     * It's to avoid human errors in code
     * @param packet
     */
    @JvmStatic
    fun registerPacketPackageFromClass(packet: Class<out Packet>) {
        registerPacketPackage(packet.getPackage().name)
    }

    fun registerPacketPackage(packageName: String) {
        val classes = Reflections(packageName).getSubTypesOf(
            Packet::class.java
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