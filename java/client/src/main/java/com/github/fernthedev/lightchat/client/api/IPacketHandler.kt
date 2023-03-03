package com.github.fernthedev.lightchat.client.api

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes

fun interface IPacketHandler {
    fun handlePacket(acceptablePacketTypes: AcceptablePacketTypes, packetId: Int)
}