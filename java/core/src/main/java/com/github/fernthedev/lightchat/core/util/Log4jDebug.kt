package com.github.fernthedev.lightchat.core.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.reflections.Reflections
import org.slf4j.Logger

object Log4jDebug {
    fun setDebug(logger: Logger?, debug: Boolean) {
        try {
            if (logger != null) Configurator.setLevel(logger.name, if (debug) Level.DEBUG else Level.INFO)
            Configurator.setLevel(Reflections::class.java.name, if (debug) Level.DEBUG else Level.WARN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}