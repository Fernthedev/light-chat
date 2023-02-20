package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.core.StaticHandler.core
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.packets.HashedPasswordPacket
import com.github.fernthedev.lightchat.core.packets.PacketJSON
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
    override suspend fun handlePacket(packetJSON: PacketJSON, clientConnection: ClientConnection, packetId: Int): Unit =
        coroutineScope {
            when (packetJSON) {
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
                    core.logger.debug("Handling message {}", packetJSON.message)
                    val chatEvent = ChatEvent(clientConnection, packetJSON.message, isCommand = false, async = true)
                    server.eventHandler.callEvent(chatEvent)

                    ServerTerminal.commandMessageParser.onCommand(chatEvent)
                }

                is CommandPacket -> {
                    val command = packetJSON.message
                    val chatEvent = ChatEvent(clientConnection, command, isCommand = true, async = true)
                    server.eventHandler.callEvent(chatEvent)
                    ServerTerminal.commandMessageParser.onCommand(chatEvent)
                }

                is AutoCompletePacket -> {
                    val candidates = ServerTerminal.autoCompleteHandler.handleLine(clientConnection, packetJSON.words)
                    packetJSON.candidateList = candidates
                    clientConnection.sendPacketLaunch(PacketTransporter(packetJSON, true))
                }

                is HashedPasswordPacket -> {
                    server.authenticationManager.attemptAuthenticationHash(
                        packetJSON.hashedPassword,
                        clientConnection
                    )
                }
            }
        }
}