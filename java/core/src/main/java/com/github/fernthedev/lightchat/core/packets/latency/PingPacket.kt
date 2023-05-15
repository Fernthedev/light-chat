package com.github.fernthedev.lightchat.core.packets.latency

import com.github.fernthedev.lightchat.core.packets.PacketInfo
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@PacketInfo(name = "PING_PACKET")
class PingPacket : PacketJSON(), LatencyPacket