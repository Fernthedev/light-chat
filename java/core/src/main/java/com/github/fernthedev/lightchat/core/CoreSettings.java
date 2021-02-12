package com.github.fernthedev.lightchat.core;

import com.github.fernthedev.lightchat.core.codecs.Codecs;
import com.github.fernthedev.lightchat.core.codecs.general.compression.CompressionAlgorithms;
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
    private int compressionLevel = 7;

    @SettingValue
    private String compressionAlgorithm = CompressionAlgorithms.JDK_ZLIB_STR;

    protected transient Charset charset = CharsetUtil.UTF_8;
    protected long timeoutTime = (long) 30 * 1000;

    protected transient String codec = DEFAULT_CODEC;

    protected static final transient String DEFAULT_CODEC = Codecs.GSON_STR;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface SettingValue {
        @NonNull String name() default "";

        boolean editable() default true;

        @NonNull String[] values() default {};
    }

    //Avoid Kotlin compile errors with Lombok
    public int getCompressionLevel() {
        return compressionLevel;
    }

    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }
}
