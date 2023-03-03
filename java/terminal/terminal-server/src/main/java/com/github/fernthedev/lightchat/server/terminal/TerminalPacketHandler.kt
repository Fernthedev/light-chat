package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.core.StaticHandler.core
import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.packets.HashedPasswordPacket
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket
import com.github.fernthedev.lightchat.server.ClientConnection
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.api.IPacketHandler
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent
import com.github.fernthedev.terminal.core.packets.AutoCompletePacket
import com.github.fernthedev.terminal.core.packets.CommandPacket
import com.github.fernthedev.terminal.core.packets.MessagePacket
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class TerminalPacketHandler(private val server: Server) : IPacketHandler {
    override suspend fun handlePacket(acceptablePacketTypes: AcceptablePacketTypes, clientConnection: ClientConnection, packetId: Int): Unit =
        coroutineScope {
            when (acceptablePacketTypes) {
                is ConnectedPacket -> {
                    if (server.settingsManager.configData.passwordRequiredForLogin) {
                        launch {
                            val authenticated = server.authenticationManager.authenticate(clientConnection).await()
                            if (!authenticated) {
                                clientConnection.sendPacketLaunch(MessagePacket("Unable to authenticate").transport())
                                clientConnection.close()
                            }
                        }
                    }
                }

                is MessagePacket -> {
                    core.logger.debug("Handling message {}", acceptablePacketTypes.message)
                    val chatEvent = ChatEvent(clientConnection, acceptablePacketTypes.message, isCommand = false, async = true)
                    server.eventHandler.callEvent(chatEvent)

                    ServerTerminal.commandMessageParser.onCommand(chatEvent)
                }

                is CommandPacket -> {
                    val command = acceptablePacketTypes.message
                    val chatEvent = ChatEvent(clientConnection, command, isCommand = true, async = true)
                    server.eventHandler.callEvent(chatEvent)
                    ServerTerminal.commandMessageParser.onCommand(chatEvent)
                }

                is AutoCompletePacket -> {
                    val candidates = ServerTerminal.autoCompleteHandler.handleLine(clientConnection, acceptablePacketTypes.words)
                    acceptablePacketTypes.candidateList = candidates
                    clientConnection.sendPacketLaunch(PacketTransporter(acceptablePacketTypes, true))
                }

                is HashedPasswordPacket -> {
                    server.authenticationManager.attemptAuthenticationHash(
                        acceptablePacketTypes.hashedPassword,
                        clientConnection
                    )
                }
            }
        }
}