package com.github.fernthedev.core.data;

import com.github.fernthedev.core.encryption.util.EncryptionUtil;
import lombok.Getter;

@Getter
public class HashedPassword {

    private String password;

    protected HashedPassword() {}

    public HashedPassword(String password) {
        this.password = EncryptionUtil.makeSHA256Hash(password);
    }
}
