package com.github.fernthedev.universal.encryption.RSA;

import com.github.fernthedev.universal.StaticHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;
import java.util.List;

public class LineEndStringEncoder extends StringEncoder {
    /**
     * Creates a new instance with the current system character set.
     */
    public LineEndStringEncoder() {
        super();
    }

    /**
     * Creates a new instance with the specified character set.
     *
     * @param charset
     */
    public LineEndStringEncoder(Charset charset) {
        super(charset);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
        super.encode(ctx, msg + StaticHandler.END_STRING, out);
    }
}
