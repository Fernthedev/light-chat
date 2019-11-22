package com.github.fernthedev.core.packets;

@PacketInfo(name = "COMMAND_PACKET")
public class CommandPacket extends Packet {

    private String message;

    public CommandPacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
