package com.github.fernthedev.core.packets.latency;

import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.PacketInfo;

@PacketInfo(name = "PONG_PACKET")
public class PongPacket extends Packet implements LatencyPacket{

}
