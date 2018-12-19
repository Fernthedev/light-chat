package com.github.fernthedev.packets;

public class CommandPacket extends Packet {

    private String message;

    public CommandPacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
