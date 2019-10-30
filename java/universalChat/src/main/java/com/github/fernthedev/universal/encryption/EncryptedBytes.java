package com.github.fernthedev.universal.encryption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EncryptedBytes {

    private final byte[] data;
    private final byte[] params;
    private final String paramAlgorithm;

}
