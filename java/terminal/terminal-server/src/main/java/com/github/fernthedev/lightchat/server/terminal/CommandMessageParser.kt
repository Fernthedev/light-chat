package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.core.ColorCode
import com.github.fernthedev.lightchat.server.Console
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent
import com.github.fernthedev.lightchat.server.terminal.exception.InvalidCommandArgumentException
import kotlinx.coroutines.Dispatchers.Default
import kotlin.coroutines.EmptyCoroutineContext

class CommandMessageParser(private val server: Server) {
    fun onCommand(e: ChatEvent) {
        val sender = e.sender
        if (e.isCancelled) return
        val runnable: Runnable
        val commandRunnable = Runnable { handleCommand(sender, e.message) } // Just a static identifier
        val messageRunnable = Runnable { handleMessage(sender, e.message) } // Just a static identifier

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
            Default.dispatch(EmptyCoroutineContext, runnable)
        } else {
            runnable.run()
        }
    }

    private fun handleCommand(sender: SenderInterface, command: String) {
        val splitString = command.split(" ".toRegex(), limit = 2).toTypedArray()
        val arguments: MutableList<String> = ArrayList()
        if (splitString.size > 1) {
            val splitArgumentsCommand = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var index = 0
            for (message in splitArgumentsCommand) {
                val message = message.replace(" {2}".toRegex(), " ")
                index++
                if (index == 1 || message == "") continue
                arguments.add(message)
            }
        }
        var mainCommand = splitString[0]
        var found = false
        mainCommand = mainCommand.replace(" {2}".toRegex(), " ")
        if (command == "") return

        try {
            if (sender !is Console) server.logger.info("[{}] /{}", sender.name, command)
            for (serverCommand in ServerTerminal.commands) {
                if (serverCommand.name.equals(mainCommand, ignoreCase = true)) {
                    found = true
                    val args: Array<String> = arguments.toTypedArray()
                    CommandWorkerThread(sender, serverCommand, args).run()
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
        private fun handleMessage(sender: SenderInterface, message: String) {
            ServerTerminal.broadcast("[" + sender!!.name + "]: " + message)
        }
    }
}