package com.github.fernthedev.lightchat.client.terminal

import com.github.fernthedev.lightchat.client.Client
import com.github.fernthedev.lightchat.client.ClientCore
import com.github.fernthedev.terminal.core.TermCore

class ClientTermCore(client: Client) : ClientCore(client), TermCore {
    override fun runCommand(command: String) {
        if (client.isRegistered) {
            ClientTerminal.messageDelay.reset()
            ClientTerminal.messageDelay.start()
            ClientTerminal.sendMessage(command)
        } else {
            ClientTerminal.logger.error("The client has not been registered yet.")
        }
    }
}