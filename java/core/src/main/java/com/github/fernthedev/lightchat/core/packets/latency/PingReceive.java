package com.github.fernthedev.lightchat.core.packets.latency;

import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.PacketInfo;

@PacketInfo(name = "PING_RECEIVE")
public class PingReceive extends Packet implements LatencyPacket{

}
