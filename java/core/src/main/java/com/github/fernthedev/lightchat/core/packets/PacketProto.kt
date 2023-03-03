package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.encryption.PacketType
import com.google.protobuf.Message

class PacketProto(override val packetName: String) : AcceptablePacketTypes {

    constructor(messageLite: Message) : this(messageLite.descriptorForType.fullName) {

    }

    override val packetType: PacketType
        get() = PacketType.PROTOBUF

}