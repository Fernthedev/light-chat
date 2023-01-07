package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.Core
import org.slf4j.Logger


open class ServerCore
@JvmOverloads
constructor(
    val server: Server,
    override val isRunning: Boolean = server.isRunning(),
    override val logger: Logger = server.logger,
    override val name: String = "Server"
) : Core {
    override fun shutdown() {
        server.shutdownServer()
    }
}