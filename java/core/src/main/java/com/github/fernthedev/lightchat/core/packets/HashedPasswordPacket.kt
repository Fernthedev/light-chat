package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.data.HashedPassword

@PacketInfo(name = "HASHED_PASSWORD_PACKET")
class HashedPasswordPacket(
    var hashedPassword: HashedPassword
) : Packet() {

    constructor(password: String) : this(HashedPassword(password))
}