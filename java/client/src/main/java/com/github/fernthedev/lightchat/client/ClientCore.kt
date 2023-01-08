package com.github.fernthedev.lightchat.client

import com.github.fernthedev.lightchat.core.Core
import org.slf4j.Logger
import kotlin.system.exitProcess

open class ClientCore(@JvmField protected var client: Client) : Core {
    override val isRunning: Boolean
        get() = client.isRunning
    override val logger: Logger
        get() = client.logger
    override val name: String
        get() = "Client"

    override fun shutdown() {
        client.disconnect()
        exitProcess(0)
    }
}