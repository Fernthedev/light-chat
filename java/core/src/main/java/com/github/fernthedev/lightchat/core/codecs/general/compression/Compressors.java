package com.github.fernthedev.lightchat.core.codecs.general.compression;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Compressors {

    public static MessageToByteEncoder<ByteBuf> getCompressEncoder(CompressionAlgorithm compressionAlgorithm, int compressionLevel) {
        switch (compressionAlgorithm) {
            case ZLIB:
                return JZLibCompressor.jZlibFrameEncoder(compressionLevel);
            case LZ4:
                boolean level = compressionLevel == 2;
                return Lz4Compressor.lz4FrameEncoder(level);
            default:
                throw new IllegalStateException("Unexpected value: " + compressionAlgorithm);
        }
    }

    public static ByteToMessageDecoder getCompressDecoder(CompressionAlgorithm compressionAlgorithm, int compressionLevel) {
        switch (compressionAlgorithm) {
            case ZLIB:
                return JZLibCompressor.jZlibDecoder();
            case LZ4:
                boolean level = compressionLevel == 2;
                return Lz4Compressor.lz4FrameDecoder(level);
            default:
                throw new IllegalStateException("Unexpected value: " + compressionAlgorithm);
        }
    }

}
