package com.github.fernthedev.lightchat.core

import com.github.fernthedev.lightchat.core.codecs.Codecs
import com.github.fernthedev.lightchat.core.codecs.general.compression.CompressionAlgorithms
import io.netty.util.CharsetUtil
import java.io.Serializable

open class CoreSettings : Serializable {
    //Avoid Kotlin compile errors with Lombok
    @SettingValue
    var compressionLevel = 7

    @SettingValue
    var compressionAlgorithm = CompressionAlgorithms.JDK_ZLIB_STR

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
        if (compressionLevel != other.compressionLevel) return false
        val `this$compressionAlgorithm`: Any = compressionAlgorithm
        val `other$compressionAlgorithm`: Any = other.compressionAlgorithm
        if (if (`this$compressionAlgorithm` == null) `other$compressionAlgorithm` != null else `this$compressionAlgorithm` != `other$compressionAlgorithm`) return false
        return if (timeoutTime != other.timeoutTime) false else true
    }

    protected open fun canEqual(other: Any?): Boolean {
        return other is CoreSettings
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + compressionLevel
        val `$compressionAlgorithm`: Any = compressionAlgorithm
        result = result * PRIME + (`$compressionAlgorithm`?.hashCode() ?: 43)
        val `$timeoutTime` = timeoutTime
        result = result * PRIME + (`$timeoutTime` ushr 32 xor `$timeoutTime`).toInt()
        return result
    }

    override fun toString(): String {
        return "CoreSettings(compressionLevel=" + compressionLevel + ", compressionAlgorithm=" + compressionAlgorithm + ", charset=" + charset + ", timeoutTime=" + timeoutTime + ", codec=" + codec + ")"
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class SettingValue(val name: String = "", val editable: Boolean = true, val values: Array<String> = [])
    companion object {
        @JvmStatic
        protected val DEFAULT_CODEC = Codecs.GSON_STR
    }
}