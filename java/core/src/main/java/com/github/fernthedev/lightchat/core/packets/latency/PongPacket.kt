package com.github.fernthedev.lightchat.core.packets.latency;

import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.PacketInfo;

@PacketInfo(name = "PONG_PACKET")
public class PongPacket extends Packet implements LatencyPacket{

}
