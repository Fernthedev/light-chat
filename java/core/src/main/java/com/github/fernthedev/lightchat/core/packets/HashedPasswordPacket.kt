package com.github.fernthedev.lightchat.core.packets

import com.github.fernthedev.lightchat.core.data.HashedPassword
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@PacketInfo(name = "HASHED_PASSWORD_PACKET")
class HashedPasswordPacket(
    var hashedPassword: HashedPassword
) : PacketJSON() {

    constructor(password: String) : this(HashedPassword(password))
}