package com.github.fernthedev.lightchat.core.packets.latency;

import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.PacketInfo;

@PacketInfo(name = "PING_PACKET")
public class PingPacket extends Packet implements LatencyPacket {

}
