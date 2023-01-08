package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.terminal.command.Command

class CommandWorkerThread(
    private val commandSender: SenderInterface,
    private val serverCommand: Command,
    private val args: Array<String>
) : Runnable {
    override fun run() {
        serverCommand.onCommand(commandSender, args)
    }
}