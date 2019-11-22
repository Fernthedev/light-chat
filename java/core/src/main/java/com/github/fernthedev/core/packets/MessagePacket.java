package com.github.fernthedev.core.packets;

@PacketInfo(name = "MESSAGE_PACKET")
public class MessagePacket extends Packet {

    public MessagePacket(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Packet Message: " + message;
    }
}
