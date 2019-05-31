package com.github.fernthedev.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SelfMessagePacket extends Packet {
    private MessageType type;

    public enum MessageType {
        FILL_PASSWORD,
        LOST_SERVER_CONNECTION,
        REGISTER_PACKET,
        TIMED_OUT_REGISTRATION
    }
}
