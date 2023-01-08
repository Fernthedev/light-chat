package com.github.fernthedev.terminal.core.packets

import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.PacketInfo

@PacketInfo(name = "COMMAND_PACKET")
class CommandPacket(@JvmField val message: String) : Packet()