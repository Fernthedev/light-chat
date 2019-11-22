package com.github.fernthedev.core.packets;

@PacketInfo(name = "TEST_CONNECT_PACKET")
public class TestConnectPacket extends Packet {

    private String message;

    public String getMessage() {
        return message;
    }

    public TestConnectPacket(String message) {
        this.message = message;
    }

}
