package com.github.fernthedev.lightchat.core.codecs.general.compression

import com.github.fernthedev.lightchat.core.StaticHandler
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.compression.SnappyFrameDecoder
import io.netty.handler.codec.compression.SnappyFrameEncoder

/**
 * To avoid classpath errors
 */
object SnappyCompressor {
    fun snappyFrameEncoder(): SnappyFrameEncoder {
        return SnappyFrameEncoder()
    }

    fun snappyDecoder(): SnappyFrameDecoder {
        return object : SnappyFrameDecoder() {
            // TODO: Remove
            @Throws(Exception::class)
            override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: List<Any>) {
                val compressed = `in`.readableBytes()
                super.decode(ctx, `in`, out)
                if (!StaticHandler.isDebug()) return

                val amount = out.filterIsInstance(ByteBuf::class.java).fold(0) { acc: Int, b ->
                    return@fold acc + b.readableBytes()
                }

                if (amount == 0) return
                StaticHandler.core.logger.debug(
                    "Compression: {}/{} ({}%}",
                    compressed,
                    amount,
                    (compressed.toDouble() / amount * 100.0).toInt()
                )
            }
        }
    }
}