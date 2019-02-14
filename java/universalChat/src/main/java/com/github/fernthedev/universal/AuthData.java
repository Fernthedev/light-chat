package com.github.fernthedev.universal;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;

@Getter
@RequiredArgsConstructor
public class AuthData {
    @NonNull
    private Cipher cipher;

    @NonNull
    private String keyPass;

}
