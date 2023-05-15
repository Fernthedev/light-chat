package com.github.fernthedev.lightchat.core.data

import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
class HashedPassword
/**
 * Encrypts the string using 256 hash
 * @param password
 */
    (
    password: String,
    hash: Boolean = true
) : Serializable {
    val password = if (hash) EncryptionUtil.makeSHA256Hash(password) else password


    companion object {
        /**
         * Does not encrypt hash
         * @param hash
         * @return
         */
        fun fromHash(hash: String): HashedPassword {
            return HashedPassword(hash, false)
        }
    }
}