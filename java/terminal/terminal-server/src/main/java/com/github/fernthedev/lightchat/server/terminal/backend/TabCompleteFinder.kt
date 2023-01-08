package com.github.fernthedev.lightchat.server.terminal.backend

import com.github.fernthedev.lightchat.core.data.LightCandidate
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal
import com.github.fernthedev.lightchat.server.terminal.command.Command
import com.github.fernthedev.lightchat.server.terminal.command.TabExecutor
import java.util.*

open class TabCompleteFinder(protected val server: Server) {
    fun handleLine(senderInterface: SenderInterface, words: List<String>): List<LightCandidate> {
        if (words.isEmpty()) return emptyList()

        if (words.size == 1) {
            return ServerTerminal.commands.map {
                val string = it.name
                LightCandidate(string, string, null, null, null, null, true)
            }
        }

        val args: List<String> = ArrayList(words.subList(1, words.size))
        val c = words[0]
        val curCommand: Command? = ServerTerminal.commands.find { it.name.equals(c, ignoreCase = true) }

        if (curCommand !is TabExecutor) return emptyList()

        val tabExecutor = curCommand as TabExecutor
        val completions = tabExecutor.getCompletions(senderInterface, LinkedList(args))

        return completions.map { string ->
            LightCandidate(string, string, null, null, null, null, true)
        }
    }
}