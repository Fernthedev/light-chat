package com.github.fernthedev.core.packets;

import lombok.Getter;
import lombok.ToString;

@Getter
@PacketInfo(name = "SELF_MESSAGE_PACKET")
@ToString
public class SelfMessagePacket extends Packet {
    private MessageType type;

    public SelfMessagePacket(MessageType type) {
        this.type = type;
    }

    public enum MessageType {
        FILL_PASSWORD,
        INCORRECT_PASSWORD_ATTEMPT, // The password attempted is wrong
        INCORRECT_PASSWORD_FAILURE, // The passwords attempted were wrong, so cancelling authentication
        LOST_SERVER_CONNECTION,
        REGISTER_PACKET,
        TIMED_OUT_REGISTRATION
    }
}
