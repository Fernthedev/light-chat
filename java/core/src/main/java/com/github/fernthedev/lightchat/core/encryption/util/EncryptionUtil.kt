package com.github.fernthedev.lightchat.core.encryption.util

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.encryption.EncryptedBytes
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.*
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec

object EncryptionUtil {
    private const val GCM_NONCE_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    fun generateSecretKey(): SecretKey {
        val generator: KeyGenerator = KeyGenerator.getInstance(StaticHandler.AES_KEY_MODE)
        generator.init(StaticHandler.AES_KEY_SIZE) // The AES key size in number of bits (256)
        return generator.generateKey()
    }


    @Throws(NoSuchAlgorithmException::class)
    fun getSecureRandom(secretKey: SecretKey): SecureRandom {
        val random = SecureRandom.getInstance("SHA1PRNG")
        random.setSeed(secretKey.encoded)
        return random
    }

    private fun getAlgorithmSpec(random: SecureRandom): GCMParameterSpec {
        // Create our IV from random bytes with the correct block size
        val nonce = ByteArray(GCM_NONCE_LENGTH)
        random.nextBytes(nonce)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
    }

    private fun getAlgorithmSpec(bytes: ByteArray): GCMParameterSpec {
        assert(bytes.size == GCM_NONCE_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, bytes)
    }

    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class)
    fun generateEncryptCipher(): Cipher = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION)


    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class)
    fun generateDecryptCipher(): Cipher = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION)


    /**
     * Encrypt object with password
     *
     * @param data   Object to be encrypted
     * @return Encrypted version of object
     */
    @Throws(
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class
    )
    fun encrypt(
        data: ByteArray,
        secret: SecretKey,
        cipherWrapper: ThreadLocal<Cipher>,
        secureRandom: SecureRandom
    ): EncryptedBytes {
        val spec = getAlgorithmSpec(secureRandom)

        val cipher = cipherWrapper.get()
        cipher.init(Cipher.ENCRYPT_MODE, secret, spec)
        val encodedData = cipher.doFinal(data)

        return EncryptedBytes(encodedData, spec.iv)
    }

    /**
     * Decrypt data with secret
     *
     * @param encryptedBytes Object to be decrypted
     * @return Decrypted version of object
     */
    @Throws(
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class
    )
    fun decrypt(
        encryptedBytes: EncryptedBytes,
        secret: SecretKey,
        cipherWrapper: ThreadLocal<Cipher>,
        nonce: ByteArray
    ): ByteArray {
        val spec = getAlgorithmSpec(nonce)

        val cipher = cipherWrapper.get()
        cipher.init(Cipher.DECRYPT_MODE, secret, spec)
        return cipher.doFinal(encryptedBytes.data)
    }

    /**
     * Hashes the string
     *
     * @param input String to be hashed
     * @return The hashed string
     */
    fun makeSHA256Hash(input: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            md.reset()
            val buffer = input.toByteArray(StandardCharsets.UTF_8)
            md.update(buffer)
            val digest = md.digest()
            val hexStr = StringBuilder()
            for (b in digest) {
                hexStr.append(((b.toInt() and 0xff) + 0x100).toString(16).substring(1))
            }
            return hexStr.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        throw IllegalStateException("Unable to create hash")
    }

}