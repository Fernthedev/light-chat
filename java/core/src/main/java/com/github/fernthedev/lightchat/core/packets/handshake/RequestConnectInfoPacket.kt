package com.github.fernthedev.lightchat.core.packets.handshake

import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.packets.PacketInfo

@PacketInfo(name = "REQUEST_CONNECT_INFO_PACKET")
class RequestConnectInfoPacket : PacketJSON()