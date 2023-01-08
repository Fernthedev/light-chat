package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

abstract class Packet protected constructor() : AcceptablePacketTypes {


    private val packetInfo: PacketInfo
        get() {
            check(javaClass.isAnnotationPresent(PacketInfo::class.java)) { "Class ${javaClass.name} does not have PacketInfo" }
            return javaClass.getAnnotation(PacketInfo::class.java)
        }

    val packetName: String
        get() = packetInfo.name

    init {
        check(javaClass.isAnnotationPresent(PacketInfo::class.java)) { "Packet must have a packet info annotation" }
    }

    override fun toString(): String {
        return "Packet(packetName=" + this.packetName + ")"
    }

    companion object {
        private const val serialVersionUID = -5039841570298012421L
        private val names: MutableMap<Class<Packet>, String> = ConcurrentHashMap()

        fun getPacketName(packet: Class<out Packet>): String {
            require(packet != Packet::class.java) { "The class cannot be " + Packet::class.java.name }
            check(Packet::class.java.isAssignableFrom(packet)) { "Packet " + packet.name + " must extend " + Packet::class.java.name }

            require(!(Modifier.isAbstract(packet.modifiers) || packet.isInterface)) { "The class cannot be abstract or interface." }
            check(packet.isAnnotationPresent(PacketInfo::class.java)) { "Packet " + packet.name + " must have a packet info annotation" }

            return packet.getAnnotation(PacketInfo::class.java).name
        }
    }
}