package com.github.fernthedev.universal;

import com.github.fernthedev.packets.*;
import com.google.protobuf.GeneratedMessageV3;

import java.util.ArrayList;
import java.util.List;

public class PacketHandler {

    public static List<GeneratedMessageV3> getPacketInstances() {
        List<GeneratedMessageV3> packets = new ArrayList<>();

        packets.add(AutoCompletePacket.getDefaultInstance());
        packets.add(ConnectedPacket.getDefaultInstance());
        packets.add(IllegalConnectionPacket.getDefaultInstance());
        packets.add(MessagePacket.getDefaultInstance());
        packets.add(RequestInfoPacket.getDefaultInstance());
        packets.add(SelfMessagePacket.getDefaultInstance());

        return packets;
    }

}
