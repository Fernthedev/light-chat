package com.github.fernthedev.core.packets;

import com.github.fernthedev.core.data.HashedPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HashedPasswordPacket extends Packet {

    private HashedPassword hashedPassword;

    public HashedPasswordPacket(String password) {
        hashedPassword = new HashedPassword(password);
    }

}
