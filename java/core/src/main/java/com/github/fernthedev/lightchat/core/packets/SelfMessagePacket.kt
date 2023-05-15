package com.github.fernthedev.lightchat.core.packets

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@PacketInfo(name = "SELF_MESSAGE_PACKET")
class SelfMessagePacket(val type: MessageType) : PacketJSON() {

    override fun toString(): String {
        return "SelfMessagePacket(type=$type)"
    }

    enum class MessageType {
        FILL_PASSWORD, INCORRECT_PASSWORD_ATTEMPT,  // The password attempted is wrong
        INCORRECT_PASSWORD_FAILURE,  // The passwords attempted were wrong, so cancelling authentication
        CORRECT_PASSWORD,  // Correct password authenticated
        LOST_SERVER_CONNECTION, REGISTER_PACKET, TIMED_OUT_REGISTRATION
    }
}