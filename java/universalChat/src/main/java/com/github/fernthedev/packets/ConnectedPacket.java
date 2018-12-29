package com.github.fernthedev.packets;

public class ConnectedPacket extends Packet {
    private String name;
    private String os;

    public ConnectedPacket(String name,String os) {
        this.name = name;
        this.os = os;
    }

    public String getName() {
        return name;
    }

    public String getOS() {
        return os;
    }
}
