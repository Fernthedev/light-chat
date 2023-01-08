package com.github.fernthedev.lightchat.client

import com.github.fernthedev.lightchat.core.api.APIUsage

@APIUsage
@Suppress("unused")
interface ILogManager {
    fun log(log: String?)
    fun logError(log: String?, e: Throwable?)
    fun info(s: String?)
    fun debug(s: String)
    fun error(s: String?)
}