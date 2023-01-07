package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.lightchat.core.packets.Packet;
import io.netty.channel.ChannelFuture;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@AllArgsConstructor
public class Console implements SenderInterface, Serializable {
    private transient Server server;

    private static final long serialVersionUID = -7832219582908962549L;

    @Nullable
    @Override
    @Deprecated
    public ChannelFuture sendPacket(Packet packet) {
        return null;
    }

    @Override
    public String getName() {
        return server.getName();
    }
}
