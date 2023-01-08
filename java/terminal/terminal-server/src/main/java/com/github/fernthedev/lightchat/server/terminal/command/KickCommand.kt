package com.github.fernthedev.lightchat.server.terminal.command

import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.server.ClientConnection
import com.github.fernthedev.lightchat.server.Console
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal
import com.github.fernthedev.terminal.core.packets.MessagePacket
import java.util.*

class KickCommand(command: String, server: Server) : Command(command), TabExecutor {
    private val server: Server

    init {
        usage = "Used to kick players using id"
        this.server = server
    }

    override fun onCommand(sender: SenderInterface, args: Array<String>) {
        if (sender is Console) {
            if (args.isEmpty()) {
                ServerTerminal.sendMessage(sender, "No player to kick?")
            } else {
                val argName = args.joinToString("")

                for (clientConnection in HashMap(
                    server!!.playerHandler.channelMap
                ).values) {
                    if (argName == clientConnection.name) {
                        clientConnection.sendObject(PacketTransporter(MessagePacket("You have been kicked."), true))
                        clientConnection.close()
                    }
                }
            }
        } else ServerTerminal.sendMessage(sender, "You don't have permission for this")
    }

    override fun getCompletions(sender: SenderInterface, args: Deque<String>): List<String> {
        val curArg = args.last
        val completions = server.playerHandler.uuidMap.values.filter { item: ClientConnection ->
            item.name.startsWith(
                curArg!!
            )
        }.toList()

        val strings: MutableList<String> = ArrayList()
        for (clientConnection in completions) {
            strings.add(clientConnection.name)
        }
        return strings
    }
}