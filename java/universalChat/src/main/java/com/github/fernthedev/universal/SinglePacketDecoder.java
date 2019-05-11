package com.github.fernthedev.universal;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

public class SinglePacketDecoder extends ProtobufDecoder {

    public @NonNull GeneratedMessageV3 thisPrototype;

    @Getter
    private static final boolean HAS_PARSER;

    static {
        boolean hasParser = false;
        try {
            // MessageLite.getParserForType() is not available until protobuf 2.5.0.
            MessageLite.class.getDeclaredMethod("getParserForType");
            hasParser = true;
        } catch (Throwable t) {
            // Ignore
        }

        HAS_PARSER = hasParser;
    }

    /**
     * Creates a new instance.
     *
     * @param prototype
     */
    public SinglePacketDecoder(@NonNull GeneratedMessageV3 prototype) {
        super(prototype);
        thisPrototype = prototype;
    }

    public SinglePacketDecoder(@NonNull GeneratedMessageV3 prototype, ExtensionRegistry extensionRegistry) {
        super(prototype, extensionRegistry);
        thisPrototype = prototype;
    }

    public SinglePacketDecoder(@NonNull GeneratedMessageV3 prototype, ExtensionRegistryLite extensionRegistry) {
        super(prototype, extensionRegistry);
        thisPrototype = prototype;
    }
    public void decode(MultiplePacketDecoder multiplePacketDecoder, ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        decode(ctx,msg,out);
    }
}
