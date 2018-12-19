package com.github.fernthedev.packets;

public class IllegalConnection extends Packet {

    private String message;

    public IllegalConnection(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
