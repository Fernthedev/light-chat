package com.github.fernthedev.server.command;

import com.google.protobuf.GeneratedMessageV3;

public interface CommandSender {


    void sendPacket(GeneratedMessageV3 packet);

    void sendMessage(String message);


    String getName();
}
