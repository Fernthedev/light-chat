package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.ServerCore
import com.github.fernthedev.terminal.core.TermCore
import kotlinx.coroutines.runBlocking

class ServerTermCore(server: Server) : ServerCore(server), TermCore {
    override fun runCommand(command: String) {
        runBlocking {
            ServerTerminal.commandHandler.dispatchCommand(command)
        }
    }
}