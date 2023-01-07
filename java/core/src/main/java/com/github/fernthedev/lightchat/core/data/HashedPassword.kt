package com.github.fernthedev.lightchat.core.data

import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import java.io.Serializable

class HashedPassword : Serializable {
    var password: String? = null
        private set

    protected constructor()

    /**
     * Encrypts the string using 256 hash
     * @param password
     */
    constructor(password: String) {
        this.password = EncryptionUtil.makeSHA256Hash(password)
    }

    companion object {
        /**
         * Does not encrypt hash
         * @param hash
         * @return
         */
        fun fromHash(hash: String?): HashedPassword {
            val hashedPassword = HashedPassword()
            hashedPassword.password = hash
            return hashedPassword
        }
    }
}