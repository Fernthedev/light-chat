package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import java.lang.reflect.Modifier

abstract class Packet protected constructor() : AcceptablePacketTypes {

    @delegate:Transient
    val packetName: String by lazy {
        javaClass.getAnnotation(PacketInfo::class.java).name
    }

    init {
        check(javaClass.isAnnotationPresent(PacketInfo::class.java)) { "Packet must have a packet info annotation" }
    }

    override fun toString(): String {
        return "Packet(packetName=" + this.packetName + ")"
    }

    companion object {
        private const val serialVersionUID = -5039841570298012421L
        fun getPacketName(packet: Class<out Packet?>): String {
            require(packet != Packet::class.java) { "The class cannot be " + Packet::class.java.name }
            check(Packet::class.java.isAssignableFrom(packet)) { "Packet " + packet.name + " must extend " + Packet::class.java.name }
            require(!(Modifier.isAbstract(packet.modifiers) || packet.isInterface)) { "The class cannot be abstract or interface." }
            check(packet.isAnnotationPresent(PacketInfo::class.java)) { "Packet " + packet.name + " must have a packet info annotation" }
            return packet.getAnnotation(PacketInfo::class.java).name
        }
    }
}