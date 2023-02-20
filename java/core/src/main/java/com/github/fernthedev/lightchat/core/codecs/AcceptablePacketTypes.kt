package com.github.fernthedev.lightchat.core.codecs

import com.github.fernthedev.lightchat.core.encryption.PacketType
import java.io.Serializable

interface AcceptablePacketTypes : Serializable {
    val packetName: String
    val packetType: PacketType
}