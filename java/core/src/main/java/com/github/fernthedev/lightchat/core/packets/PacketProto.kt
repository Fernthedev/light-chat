package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.encryption.PacketType
import com.google.protobuf.Message
import java.io.Serializable

class PacketProto(@Suppress("MemberVisibilityCanBePrivate") val message: Message) : AcceptablePacketTypes, Serializable {
    override val packetName: String
        get() = message.descriptorForType.fullName


    override val packetType: PacketType
        get() = PacketType.PROTOBUF

}