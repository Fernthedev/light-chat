package com.github.fernthedev.packets;

public class TestConnectPacket extends Packet {

    private String message;

    public String getMessage() {
        return message;
    }

    public TestConnectPacket(String message) {
        this.message = message;
    }

}
