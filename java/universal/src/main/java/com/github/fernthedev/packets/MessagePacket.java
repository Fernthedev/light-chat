package com.github.fernthedev.packets;

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
        return message;
    }
}
