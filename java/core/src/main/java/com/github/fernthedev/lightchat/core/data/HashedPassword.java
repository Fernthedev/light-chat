package com.github.fernthedev.lightchat.core.data;

import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class HashedPassword implements Serializable {

    private String password;

    protected HashedPassword() {}

    /**
     * Encrypts the string using 256 hash
     * @param password
     */
    public HashedPassword(String password) {
        this.password = EncryptionUtil.makeSHA256Hash(password);
    }

    /**
     * Does not encrypt hash
     * @param hash
     * @return
     */
    public static HashedPassword fromHash(String hash) {
        HashedPassword hashedPassword = new HashedPassword();

        hashedPassword.password = hash;

        return hashedPassword;
    }
}
