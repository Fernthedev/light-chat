package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.encryption.PacketType
import com.squareup.moshi.Json
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

abstract class PacketJSON protected constructor() : AcceptablePacketTypes {
    @Json(ignore = true)
    private val packetInfo: PacketInfo
        get() {
            check(javaClass.isAnnotationPresent(PacketInfo::class.java)) { "Class ${javaClass.name} does not have PacketInfo" }
            return javaClass.getAnnotation(PacketInfo::class.java)
        }

    override val packetName: String
        get() = packetInfo.name

    override val packetType: PacketType
        get() = PacketType.JSON

    init {
        check(javaClass.isAnnotationPresent(PacketInfo::class.java)) { "Packet must have a packet info annotation" }
    }

    override fun toString(): String {
        return "Packet(packetName=" + this.packetName + ")"
    }

    companion object {
        private const val serialVersionUID = -5039841570298012421L
        private val names: MutableMap<Class<PacketJSON>, String> = ConcurrentHashMap()

        fun getPacketName(packetJSON: Class<out PacketJSON>): String {
            require(packetJSON != PacketJSON::class.java) { "The class cannot be " + PacketJSON::class.java.name }
            check(PacketJSON::class.java.isAssignableFrom(packetJSON)) { "Packet " + packetJSON.name + " must extend " + PacketJSON::class.java.name }

            require(!(Modifier.isAbstract(packetJSON.modifiers) || packetJSON.isInterface)) { "The class cannot be abstract or interface." }
            check(packetJSON.isAnnotationPresent(PacketInfo::class.java)) { "Packet " + packetJSON.name + " must have a packet info annotation" }

            return packetJSON.getAnnotation(PacketInfo::class.java).name
        }
    }
}