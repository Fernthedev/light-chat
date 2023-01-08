package com.github.fernthedev.lightchat.core

import com.github.fernthedev.lightchat.core.codecs.Codecs
import io.netty.util.CharsetUtil
import java.io.Serializable

open class CoreSettings : Serializable {

    @Transient
    var charset = CharsetUtil.UTF_8
    var timeoutTime = 30L * 1000

    @Transient
    var codec = DEFAULT_CODEC
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is CoreSettings) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        return timeoutTime == other.timeoutTime
    }

    protected open fun canEqual(other: Any?): Boolean {
        return other is CoreSettings
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result *= PRIME
        val `$timeoutTime` = timeoutTime
        result = result * PRIME + (`$timeoutTime` ushr 32 xor `$timeoutTime`).toInt()
        return result
    }

    override fun toString(): String {
        return "CoreSettings(charset=$charset, timeoutTime=$timeoutTime, codec=$codec)"
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class SettingValue(val name: String = "", val editable: Boolean = true, val values: Array<String> = [])
    companion object {
        @JvmStatic
        protected val DEFAULT_CODEC = Codecs.GSON_STR
    }
}