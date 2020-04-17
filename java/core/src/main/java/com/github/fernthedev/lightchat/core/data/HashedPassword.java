package com.github.fernthedev.core.data;

import com.github.fernthedev.core.encryption.util.EncryptionUtil;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class HashedPassword implements Serializable {

    private String password;

    protected HashedPassword() {}

    public HashedPassword(String password) {
        this.password = EncryptionUtil.makeSHA256Hash(password);
    }
}
