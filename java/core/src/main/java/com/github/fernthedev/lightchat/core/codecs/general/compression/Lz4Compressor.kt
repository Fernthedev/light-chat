package com.github.fernthedev.lightchat.core.codecs.general.compression;

import com.github.fernthedev.lightchat.core.StaticHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameEncoder;

import java.util.List;

/**
 * To avoid classpath errors
 */
public class Lz4Compressor {

    public static Lz4FrameEncoder lz4FrameEncoder(boolean level) {
        return new Lz4FrameEncoder(level);
    }

    public static Lz4FrameDecoder lz4FrameDecoder(boolean checksums) {
        return new Lz4FrameDecoder(checksums) {

            // TODO: Remove
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                int compressed = in.readableBytes();
                super.decode(ctx, in, out);

                if (StaticHandler.isDebug()) {
                    int amount = 0;

                    for (Object o : out) {
                        if (o instanceof ByteBuf) {
                            amount += ((ByteBuf) o).readableBytes();
                        }
                    }

                    if (amount == 0) return;

                    StaticHandler.getCore().getLogger().debug("Compression: {}/{} ({}%}", compressed, amount, (int) (((double) compressed / amount) * 100.0));
                }
            }
        };
    }

}
