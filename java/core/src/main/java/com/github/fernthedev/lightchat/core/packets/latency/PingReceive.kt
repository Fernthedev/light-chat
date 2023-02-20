package com.github.fernthedev.lightchat.core.packets.latency

import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.packets.PacketInfo

@PacketInfo(name = "PING_RECEIVE")
class PingReceive : PacketJSON(), LatencyPacket