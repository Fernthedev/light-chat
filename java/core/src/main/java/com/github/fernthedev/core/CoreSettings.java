package com.github.fernthedev.core;

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
    private transient Charset charset = CharsetUtil.UTF_8;
    private long timeoutTime = (long) 30 * 1000;
}
