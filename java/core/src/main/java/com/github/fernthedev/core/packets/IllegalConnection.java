package com.github.fernthedev.core.packets;

@PacketInfo(name = "ILLEGAL_CONNECTION")
public class IllegalConnection extends Packet {

    private String message;

    public IllegalConnection(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
