package com.github.fernthedev.lightchat.core.codecs.general.compression

import com.github.fernthedev.lightchat.core.CoreSettings
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.ByteToMessageDecoder
import java.util.HashMap
import io.netty.handler.codec.compression.*
import java.util.function.Function

typealias CompressPair = Pair<Function<CoreSettings, out MessageToByteEncoder<*>?>, Function<CoreSettings, out ByteToMessageDecoder?>>


object CompressionAlgorithms {
    private val compressionAlgorithmMap: MutableMap<String, CompressPair> = HashMap()

    const val ZLIB_STR = "ZLIB"

    @JvmField
    val ZLIB = CompressPair(
        { s: CoreSettings -> JZLibCompressor.jZlibFrameEncoder(s.compressionLevel) },
        { JZLibCompressor.jZlibDecoder() })

    const val JDK_ZLIB_STR = "JDK_ZLIB"

    @JvmField
    val JDK_ZLIB = CompressPair(
        { s: CoreSettings -> JdkZlibEncoder(s.compressionLevel) },
        { JdkZlibDecoder() })

    const val LZ4_STR = "LZ4"
    @JvmField
    val LZ4 = CompressPair(
        { s: CoreSettings -> Lz4Compressor.lz4FrameEncoder(s.compressionLevel == 2) },
        { s: CoreSettings -> Lz4Compressor.lz4FrameDecoder(s.compressionLevel == 2) })

    @JvmStatic
    fun getCompressions(s: String): CompressPair {
        return compressionAlgorithmMap[s.toLowerCase()]!!
    }

    @JvmStatic
    fun registerCompression(
        s: String,
        compression: CompressPair
    ): String {
        val s1 = s.toLowerCase()
        compressionAlgorithmMap[s1] = compression
        return s1
    }

    init {
        registerCompression(ZLIB_STR, ZLIB)
        registerCompression(LZ4_STR, LZ4)
        registerCompression(JDK_ZLIB_STR, JDK_ZLIB)
    }
}