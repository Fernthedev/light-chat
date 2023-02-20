package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.core.ColorCode
import com.github.fernthedev.lightchat.core.StaticHandler.isDebug
import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.packets.IllegalConnectionPacket
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket
import com.github.fernthedev.lightchat.core.packets.handshake.KeyResponsePacket
import com.github.fernthedev.lightchat.core.packets.handshake.RequestConnectInfoPacket
import com.github.fernthedev.lightchat.core.packets.latency.PingPacket
import com.github.fernthedev.lightchat.core.packets.latency.PingReceive
import com.github.fernthedev.lightchat.core.packets.latency.PongPacket
import com.github.fernthedev.lightchat.server.ClientConnection
import com.github.fernthedev.lightchat.server.Console
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.terminal.command.Command
import com.github.fernthedev.lightchat.server.terminal.command.KickCommand
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent
import com.github.fernthedev.terminal.core.packets.MessagePacket
import io.netty.channel.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class ServerCommandHandler(private val server: Server) {
    init {
        server.logger.info("CommandHandler created")
        registerCommands()
    }

    @APIUsage
    suspend fun dispatchCommand(command: String) = coroutineScope {
        val commandSplit = command.replace(" {2}".toRegex(), "").trim { it <= ' ' }
        require(!(commandSplit == "" || commandSplit == " ")) { "Command cannot be \"\"" }

        launch {
            val chatEvent = ChatEvent(server.console, commandSplit, isCommand = true, async = true)
            server.eventHandler.callEvent(chatEvent)
            ServerTerminal.commandMessageParser.onCommand(chatEvent)
        }
    }

    @APIUsage
    suspend fun dispatchCommand(sender: SenderInterface, command: String) = coroutineScope {
        val splitCommand = command.replace(" {2}".toRegex(), "").trim { it <= ' ' }
        require(!(splitCommand == "" || splitCommand == " ")) { "Command cannot be \"\"" }

        launch {
            val chatEvent = ChatEvent(sender, splitCommand, isCommand = true, async = true)
            server.eventHandler.callEvent(chatEvent)
            ServerTerminal.commandMessageParser.onCommand(chatEvent)
        }
    }

    private fun registerCommands() {
        ServerTerminal.registerCommand(object : Command("exit") {
            override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                when (sender) {
                    is Console -> {
                        ServerTerminal.sendMessage(sender, "Exiting")
                        server.shutdownServer()
                        exitProcess(0)
                    }

                    is ClientConnection -> {
                        sender.close()
                    }
                }
            }
        }).usage = "Safely closes the server."
        ServerTerminal.registerCommand(object : Command("stop") {
            override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                when (sender) {
                    is Console -> {
                        ServerTerminal.sendMessage(sender, "Exiting")
                        server.shutdownServer()
                        exitProcess(0)
                    }

                    is ClientConnection -> {
                        sender.close()
                    }
                }
            }
        }).usage = "Safely closes the server."
        ServerTerminal.registerCommand(object : Command("broadcast") {
            override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                when (sender) {
                    is Console -> {
                        if (args.isNotEmpty()) {
                            val argString = StringBuilder()
                            var index = 0
                            for (arg in args) {
                                index++
                                if (index != 1) {
                                    argString.append(" ")
                                }
                                argString.append(arg)
                            }
                            val message = argString.toString()
                            ServerTerminal.broadcast("[Server]: $message")
                        } else {
                            ServerTerminal.sendMessage(sender, "No message?")
                        }
                    }

                    else -> ServerTerminal.sendMessage(sender, "You don't have permission for this")
                }
            }
        }).usage = "Sends a broadcast message to all clients"
        ServerTerminal.registerCommand(object : Command("ping") {
            override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                if (sender is Console) {
                    server.playerHandler.channelMap.forEach { (_: Channel?, connection: ClientConnection) -> connection.ping() }
                }
                if (sender is ClientConnection) {
                    sender.ping()
                }
            }
        }).usage = "Sends a ping packet to all clients"
        ServerTerminal.registerCommand(object : Command("list") {
            override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                if (sender is Console) {
                    ServerTerminal.sendMessage(sender, "Players: (${server.playerHandler.uuidMap.size})")
                    for (clientConnection in HashMap(
                        server.playerHandler.channelMap
                    ).values) {
                        ServerTerminal.sendMessage(
                            sender,
                            "${clientConnection.name} :${clientConnection.uuid} {${clientConnection.address}} [${clientConnection.os}/${clientConnection.langFramework}] Ping:" + clientConnection.getPingDelay(
                                TimeUnit.MILLISECONDS
                            ) + "ms"
                        )
                    }
                }
                if (sender is ClientConnection) {
                    var message = "Players: (${server.playerHandler.uuidMap.size - 1})"
                    for (clientConnection in HashMap(
                        server.playerHandler.channelMap
                    ).values) {
                        if (clientConnection == null) continue
                        message = """
                        
                        ${clientConnection.name}[${clientConnection.os}/${clientConnection.langFramework}] Ping:${
                            clientConnection.getPingDelay(
                                TimeUnit.MILLISECONDS
                            )
                        }ms
                        """.trimIndent()

                        // ServerTerminal.sendMessage(sender, clientConnection.getName() + " :" + clientConnection.getId() + " Ping:" + clientConnection.getDelayTime() + "ms");
                    }
                    ServerTerminal.sendMessage(sender, message)
                }
            }
        }).usage = "Lists all players with ip, id and name"
        if (isDebug()) {
            ServerTerminal.registerCommand(object : Command("testpackets") {
                override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                    val connections: MutableList<ClientConnection> = ArrayList()
                    if (sender is Console) {
                        connections.addAll(server.playerHandler.channelMap.values)
                    }
                    if (sender is ClientConnection) {
                        connections.add(sender)
                    }
                    for (connection in connections) {
                        ServerTerminal.sendMessage(
                            sender,
                            ColorCode.YELLOW.toString() + "Running packets on " + connection.name
                        )
                        connection.sendPacketLaunch(MessagePacket("test").transport(true))
                        connection.sendPacketLaunch(PingPacket().transport(true))
                        connection.sendPacketLaunch(PingReceive().transport(true))
                        connection.sendPacketLaunch(PongPacket().transport(true))
                        if (connection.tempKeyPair != null) {
                            connection.sendPacketLaunch(
                                InitialHandshakePacket(
                                    connection.tempKeyPair!!.public,
                                    connection.versionData
                                )
                                    .transport(true)
                            )
                            connection.sendPacketLaunch(
                                KeyResponsePacket(
                                    connection.secretKey!!,
                                    connection.tempKeyPair!!.public
                                )
                                    .transport(true)
                            )
                        }
                        connection.sendPacketLaunch(RequestConnectInfoPacket().transport(true))
                        connection.sendPacketLaunch(
                            ConnectedPacket(
                                connection.name,
                                connection.os,
                                connection.versionData,
                                connection.langFramework
                            )
                                .transport(true)
                        )
                        for (messageType in SelfMessagePacket.MessageType.values()) {
                            connection.sendPacketLaunch(SelfMessagePacket(messageType).transport(true))
                        }
                        connection.sendPacketLaunch(IllegalConnectionPacket("test packet").transport(true))
                    }
                }
            })
        }
        ServerTerminal.registerCommand(KickCommand("kick", server))
        ServerTerminal.registerCommand(object : Command("ban") {
            override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                if (sender is Console) {
                    if (args.size <= 1) {
                        ServerTerminal.sendMessage(
                            sender,
                            "No player to kick or type? (ban {type} {player}) \n types: name,ip"
                        )
                    } else {
                        val message = StringBuilder()
                        var index = 0
                        for (messageCheck in args) {
                            index++
                            if (index >= 2) {
                                message.append(messageCheck)
                            }
                        }
                        when (args[0].lowercase(Locale.getDefault())) {
                            "name" -> for (clientConnection in HashMap(
                                server.playerHandler.channelMap
                            ).values) {
                                if (clientConnection.name == args[1]) {
                                    clientConnection.sendPacketLaunch(
                                        PacketTransporter(
                                            MessagePacket("Banned: $message"),
                                            true
                                        )
                                    )
                                    clientConnection.close()
                                    server.banManager.ban(clientConnection.address!!)
                                    break
                                }
                            }

                            "ip" -> {
                                for (clientConnection in HashMap(
                                    server.playerHandler.channelMap
                                ).values) {
                                    if (clientConnection.address == args[1]) {
                                        clientConnection.sendPacketLaunch(
                                            PacketTransporter(
                                                MessagePacket("Banned: $message"),
                                                true
                                            )
                                        )
                                        clientConnection.close()
                                    }
                                }
                                server.banManager.ban(args[1])
                            }

                            else -> {
                                ServerTerminal.sendMessage(sender, "Unknown argument " + args[0])
                                return
                            }
                        }
                        ServerTerminal.sendMessage(sender, ColorCode.GREEN.toString() + "Banned " + args[1])
                    }
                } else ServerTerminal.sendMessage(sender, "You don't have permission for this")
            }
        }).usage = "Used to ban players using id. "
        ServerTerminal.registerCommand(object : Command("help") {
            override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
                if (args.isEmpty()) {
                    ServerTerminal.sendMessage(sender, "Following commands: ")
                    for (serverCommand in ServerTerminal.commands) {
                        ServerTerminal.sendMessage(sender, serverCommand.name)
                    }
                } else {
                    val command = args[0]
                    var executed = false
                    for (serverCommand in ServerTerminal.commands) {
                        if (serverCommand.name.equals(command, ignoreCase = true)) {
                            if (serverCommand.usage == "") {
                                ServerTerminal.sendMessage(sender, "No usage found.")
                            } else ServerTerminal.sendMessage(sender, "Usage: \n" + serverCommand.usage)
                            executed = true
                            break
                        }
                    }
                    if (!executed) ServerTerminal.sendMessage(sender, "No such command found for getting help")
                }
            }
        }).usage = "Shows list of commands or usage of a command"
    }
}