package com.github.fernthedev.server;

import com.github.fernthedev.core.packets.Packet;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
public class Console implements SenderInterface, Serializable {
    private transient Server server;

    private static final long serialVersionUID = -7832219582908962549L;

    @Override
    public void sendPacket(Packet packet) { }

    @Override
    public String getName() {
        return server.getName();
    }
}
