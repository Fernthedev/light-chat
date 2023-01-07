package com.github.fernthedev.lightchat.core.packets

@PacketInfo(name = "ILLEGAL_CONNECTION_PACKET")
class IllegalConnectionPacket(val message: String) : Packet()