package com.github.fernthedev.lightchat.server.terminal.command

import com.github.fernthedev.lightchat.server.SenderInterface
import java.util.*

interface TabExecutor {
    /**
     * Returns a list of completions based on the arguments given
     * @param sender
     * @param args
     * @return
     */
    fun getCompletions(sender: SenderInterface, args: Deque<String>): List<String>
}