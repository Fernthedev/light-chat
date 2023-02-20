package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.terminal.command.Command
import kotlinx.coroutines.coroutineScope

class CommandWorkerThread(
    private val commandSender: SenderInterface,
    private val serverCommand: Command,
    private val args: Array<String>
) {
    suspend fun run() = coroutineScope {
        serverCommand.onCommand(commandSender, args)
    }
}