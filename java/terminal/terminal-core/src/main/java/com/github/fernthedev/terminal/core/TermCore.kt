package com.github.fernthedev.terminal.core

import com.github.fernthedev.lightchat.core.Core

interface TermCore : Core {
    fun runCommand(command: String)
}