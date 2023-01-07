package com.github.fernthedev.lightchat.core

import org.slf4j.Logger

interface Core {
    val isRunning: Boolean
    val logger: Logger
    val name: String
    fun shutdown()
}