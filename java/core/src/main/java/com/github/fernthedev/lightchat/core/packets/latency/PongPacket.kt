package com.github.fernthedev.lightchat.core.packets.latency

import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.PacketInfo

@PacketInfo(name = "PONG_PACKET")
class PongPacket : Packet(), LatencyPacket