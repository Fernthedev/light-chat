package com.github.fernthedev.lightchat.client

import com.github.fernthedev.lightchat.core.StaticHandler.isDebug
import org.slf4j.Logger

class CLogger(private val logger: Logger) : ILogManager {
    override fun log(log: String?) {
        logger.info(log)
    }

    override fun logError(log: String?, e: Throwable?) {
        logger.error(log, e)
    }

    override fun info(s: String?) {
        log(s)
    }

    override fun debug(s: String) {
        if (isDebug()) {
            log("[DEBUG] $s")
        }
    }

    override fun error(s: String?) {
        logger.error(s)
    }
}