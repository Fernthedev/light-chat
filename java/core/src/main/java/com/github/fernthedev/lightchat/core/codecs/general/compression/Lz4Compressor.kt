package com.github.fernthedev.lightchat.core.codecs.general.compression

import com.github.fernthedev.lightchat.core.StaticHandler
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.compression.Lz4FrameDecoder
import io.netty.handler.codec.compression.Lz4FrameEncoder

/**
 * To avoid classpath errors
 */
object Lz4Compressor {
    fun lz4FrameEncoder(level: Boolean): Lz4FrameEncoder {
        return Lz4FrameEncoder(level)
    }

    fun lz4FrameDecoder(checksums: Boolean): Lz4FrameDecoder {
        return object : Lz4FrameDecoder(checksums) {
            // TODO: Remove
            @Throws(Exception::class)
            override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: List<Any>) {
                val compressed = `in`.readableBytes()
                super.decode(ctx, `in`, out)
                if (StaticHandler.isDebug()) {
                    var amount = 0
                    for (o in out) {
                        if (o is ByteBuf) {
                            amount += o.readableBytes()
                        }
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
}