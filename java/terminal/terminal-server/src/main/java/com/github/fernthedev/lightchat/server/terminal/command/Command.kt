package com.github.fernthedev.lightchat.server.terminal.command

import com.github.fernthedev.lightchat.server.SenderInterface
import java.util.function.Consumer

abstract class Command(val name: String) {
    var usage = ""

    abstract fun onCommand(sender: SenderInterface, args: Array<String>)

    /**
     * Allows you to make autocomplete only suggest based off what is written
     * @param arg The argument currently used
     * @param possibilities All of the possibilities
     * @return The auto-complete possibilities
     */
    fun search(arg: String, possibilities: List<String>): List<String> {
        val newPos: MutableList<String> = ArrayList()
        possibilities.forEach(Consumer { s: String ->
            if (s.startsWith(arg) || s.contains(
                    arg
                )
            ) {
                newPos.add(s)
            }
        })
        return newPos
    }
}