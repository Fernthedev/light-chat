package com.github.fernthedev.core.packets;

import lombok.Getter;

@Getter
@PacketInfo(name = "SELF_MESSAGE_PACKET")
public class SelfMessagePacket extends Packet {
    private MessageType type;

    public SelfMessagePacket(MessageType type) {
        this.type = type;
    }

    public enum MessageType {
        FILL_PASSWORD,
        LOST_SERVER_CONNECTION,
        REGISTER_PACKET,
        TIMED_OUT_REGISTRATION
    }
}
