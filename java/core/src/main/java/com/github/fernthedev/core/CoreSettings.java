package com.github.fernthedev.core;

import com.github.fernthedev.core.encryption.codecs.CodecEnum;
import com.github.fernthedev.core.encryption.codecs.JSONHandler;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.nio.charset.Charset;

@Data
@Setter
@Getter
@ToString
public class CoreSettings implements Serializable {
    protected transient Charset charset = CharsetUtil.UTF_8;
    protected long timeoutTime = (long) 30 * 1000;

    protected transient JSONHandler codec = DEFAULT_CODEC.getJsonHandler();

    protected static final transient CodecEnum DEFAULT_CODEC = CodecEnum.GSON;
}
