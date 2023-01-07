package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.data.HashedPassword

@PacketInfo(name = "HASHED_PASSWORD_PACKET")
class HashedPasswordPacket : Packet {
    var hashedPassword: HashedPassword
        private set

    constructor(password: String) {
        hashedPassword = HashedPassword(password)
    }

    constructor(hashedPassword: HashedPassword) {
        this.hashedPassword = hashedPassword
    }
}