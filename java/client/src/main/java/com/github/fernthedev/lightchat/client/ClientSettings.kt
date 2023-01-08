package com.github.fernthedev.lightchat.client

import com.github.fernthedev.lightchat.core.CoreSettings

open class ClientSettings : CoreSettings() {
    var isRunNatives = true
    override fun toString(): String {
        return "ClientSettings(runNatives=" + isRunNatives + ")"
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ClientSettings) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (!super.equals(o)) return false
        return if (isRunNatives != other.isRunNatives) false else true
    }

    override fun canEqual(other: Any?): Boolean {
        return other is ClientSettings
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = super.hashCode()
        result = result * PRIME + if (isRunNatives) 79 else 97
        return result
    }
}