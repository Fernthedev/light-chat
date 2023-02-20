package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.core.ColorCode
import com.github.fernthedev.lightchat.server.Console
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent
import com.github.fernthedev.lightchat.server.terminal.exception.InvalidCommandArgumentException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CommandMessageParser(private val server: Server) {
    suspend fun onCommand(e: ChatEvent) = coroutineScope {
        val sender = e.sender
        if (e.isCancelled) return@coroutineScope
        val runnable: suspend () -> Unit
        val commandRunnable = suspend { handleCommand(sender, e.message) } // Just a static identifier
        val messageRunnable = suspend { handleMessage(sender, e.message) } // Just a static identifier

        runnable = if (e.sender is Console) {
            commandRunnable
        } else {
            if (e.isCommand) {
                commandRunnable
            } else {
                messageRunnable
            }
        }
        if (e.isAsynchronous) {
            launch {
                runnable()
            }
        } else {
            runnable()
        }
    }

    private suspend fun handleCommand(sender: SenderInterface, command: String) = coroutineScope{
        val splitString = command.split(" ".toRegex(), limit = 2).toTypedArray()
        val arguments: MutableList<String> = ArrayList()
        if (splitString.size > 1) {
            val splitArgumentsCommand = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var index = 0
            for (message in splitArgumentsCommand) {
                val message2 = message.replace(" {2}".toRegex(), " ")
                index++
                if (index == 1 || message2 == "") continue
                arguments.add(message2)
            }
        }
        var mainCommand = splitString[0]
        var found = false
        mainCommand = mainCommand.replace(" {2}".toRegex(), " ")
        if (command == "") return@coroutineScope

        try {
            if (sender !is Console) server.logger.info("[{}] /{}", sender.name, command)
            for (serverCommand in ServerTerminal.commands) {
                if (serverCommand.name.equals(mainCommand, ignoreCase = true)) {
                    found = true
                    val args: Array<String> = arguments.toTypedArray()

                    serverCommand.onCommand(sender, args)
                    break
                }
            }
        } catch (e: InvalidCommandArgumentException) {
            ServerTerminal.sendMessage(sender, ColorCode.RED.toString() + "Error: " + e.message)
        } catch (e: Exception) {
            server.logger.error(e.message, e)
            ServerTerminal.sendMessage(
                sender,
                ColorCode.RED.toString() + "Command exception occurred. Error: " + e.message
            )
        }

        if (!found) {
            ServerTerminal.sendMessage(sender, ColorCode.RED.toString() + "No such command found")
        }
    }

    companion object {
        private suspend fun handleMessage(sender: SenderInterface, message: String) {
            ServerTerminal.broadcast("[" + sender!!.name + "]: " + message)
        }
    }
}