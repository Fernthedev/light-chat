package com.github.fernthedev.lightchat.core.packets

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@PacketInfo(name = "ILLEGAL_CONNECTION_PACKET")
class IllegalConnectionPacket(val message: String) : PacketJSON()