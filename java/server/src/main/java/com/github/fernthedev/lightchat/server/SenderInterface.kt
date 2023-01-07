package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.lightchat.core.packets.Packet;
import io.netty.channel.ChannelFuture;
import org.jetbrains.annotations.Nullable;

public interface SenderInterface {
    @Nullable
    ChannelFuture sendPacket(Packet packet);

    String getName();
}
