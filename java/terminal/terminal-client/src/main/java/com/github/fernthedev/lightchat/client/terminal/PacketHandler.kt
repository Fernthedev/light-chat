package com.github.fernthedev.lightchat.client.terminal

import com.github.fernthedev.lightchat.client.api.IPacketHandler
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent
import com.github.fernthedev.lightchat.core.ColorCode
import com.github.fernthedev.lightchat.core.StaticHandler.core
import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket
import com.github.fernthedev.terminal.core.packets.AutoCompletePacket
import com.github.fernthedev.terminal.core.packets.MessagePacket
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class PacketHandler : IPacketHandler {
    override fun handlePacket(acceptablePacketTypes: AcceptablePacketTypes, packetId: Int) {
        when (acceptablePacketTypes) {
            is AutoCompletePacket -> {
                ClientTerminal.autoCompleteHandler.addCandidates(acceptablePacketTypes.candidateList)
            }

            is MessagePacket -> {
                ClientTerminal.logger.info(acceptablePacketTypes.message)
                if (ClientTerminal.messageDelay.isRunning) ClientTerminal.messageDelay.stop()
                ClientTerminal.logger.debug(
                    "Time taken for message: {}", ClientTerminal.messageDelay.elapsed(
                        TimeUnit.MILLISECONDS
                    )
                )
            }

            is SelfMessagePacket -> {
                if (Objects.requireNonNull(acceptablePacketTypes.type) === SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE) {
                    ClientTerminal.logger.error(ColorCode.RED.toString() + "Failed all attempts to login.")
                }
            }
        }
    }

    fun onDisconnect(e: ServerDisconnectEvent?) {
        core.logger.info("CLOSING CLIENT")
        exitProcess(0)
    }
}