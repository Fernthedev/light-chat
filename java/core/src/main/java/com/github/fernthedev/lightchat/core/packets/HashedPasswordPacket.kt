package com.github.fernthedev.lightchat.core.packets;

import com.github.fernthedev.lightchat.core.data.HashedPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@PacketInfo(name = "HASHED_PASSWORD_PACKET")
public class HashedPasswordPacket extends Packet {

    private HashedPassword hashedPassword;

    public HashedPasswordPacket(String password) {
        hashedPassword = new HashedPassword(password);
    }

}
