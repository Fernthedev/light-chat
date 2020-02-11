package com.github.fernthedev.terminal.core;

import com.github.fernthedev.core.PacketRegistry;
import com.github.fernthedev.terminal.core.packets.MessagePacket;

public class CommonUtil {

    public static void registerTerminalPackets() {
        PacketRegistry.registerPacketPackageFromPacket(new MessagePacket(""));
    }

}
