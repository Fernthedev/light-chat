package com.github.fernthedev.terminal.core.packets;

import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.PacketInfo;

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
