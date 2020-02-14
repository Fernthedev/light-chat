package com.github.fernthedev.core.encryption.codecs;

public interface JSONHandler {
    <T> T fromJson(String decodedStr, Class<T> packetWrapperClass);

    String toJson(Object msg);
}
