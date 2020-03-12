package com.github.fernthedev.core.packets;

@PacketInfo(name = "ILLEGAL_CONNECTION_PACKET")
public class IllegalConnectionPacket extends Packet {

    private String message;

    public IllegalConnectionPacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
