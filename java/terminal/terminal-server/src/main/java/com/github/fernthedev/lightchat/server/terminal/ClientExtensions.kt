package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.lightchat.server.SenderInterface

fun SenderInterface.sendMessage(s: String) {
    ServerTerminal.sendMessage(this, s)
}