package com.github.fernthedev.lightchat.core;

import com.github.fernthedev.lightchat.core.codecs.CodecEnum;
import com.github.fernthedev.lightchat.core.codecs.JSONHandler;
import io.netty.util.CharsetUtil;
import lombok.*;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;

@Data
@Setter
@Getter
@ToString
public class CoreSettings implements Serializable {

    @SettingValue
    private int compression = 2;

    protected transient Charset charset = CharsetUtil.UTF_8;
    protected long timeoutTime = (long) 30 * 1000;

    protected transient JSONHandler codec = DEFAULT_CODEC.getJsonHandler();

    protected static final transient CodecEnum DEFAULT_CODEC = CodecEnum.GSON;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface SettingValue {
        @NonNull String name() default "";

        boolean editable() default true;

        @NonNull String[] values() default {};
    }
}
