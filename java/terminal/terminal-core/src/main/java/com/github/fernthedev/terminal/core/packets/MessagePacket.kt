package com.github.fernthedev.terminal.core.packets

import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.packets.PacketInfo

@PacketInfo(name = "MESSAGE_PACKET")
class MessagePacket(@JvmField val message: String) : PacketJSON() {

    override fun toString(): String {
        return "Packet Message: $message"
    }
}