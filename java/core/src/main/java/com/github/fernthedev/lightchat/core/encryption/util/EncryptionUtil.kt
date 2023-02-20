package com.github.fernthedev.lightchat.core.encryption.util

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.encryption.EncryptedBytes
import java.io.IOException
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.AlgorithmParameterSpec
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

    /**
     * Encrypt object with password
     *
     * @param object Object to be encrypted
     * @param secret Password to use for encryption
     * @return Encrypted version of object
     */
    @Deprecated("")
    fun encrypt(`object`: Serializable?, secret: SecretKey?): SealedObject? {
        try {
            val cipher = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secret)


            // properly encode the complete ciphertext
            //logEncrypt(password, object);
            return SealedObject(`object`, cipher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(NoSuchAlgorithmException::class)
    fun getSecureRandom(secretKey: SecretKey): SecureRandom {
        val random = SecureRandom.getInstance("SHA1PRNG")
        random.setSeed(secretKey.encoded)
        return random
    }

    private fun getAlgorithmSpec(random: SecureRandom): AlgorithmParameterSpec {
        // Create our IV from random bytes with the correct block size
        val nonce = ByteArray(GCM_NONCE_LENGTH)
        random.nextBytes(nonce)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
    }

    @get:Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class)
    val encryptCipher: Cipher
        /**
         * Initializes and creates an encryption cipher
         * @return the cipher
         *
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         */
        get() = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION)

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
    fun encrypt(data: ByteArray, secret: SecretKey, cipher: Cipher, secureRandom: SecureRandom): EncryptedBytes {
        val spec = getAlgorithmSpec(secureRandom)
        cipher.init(Cipher.ENCRYPT_MODE, secret, spec)
        val encodedData = cipher.doFinal(data)
        val params = cipher.parameters.encoded
        val paramAlgorithm = cipher.parameters.algorithm
        return EncryptedBytes(encodedData, params, paramAlgorithm)
    }

    @get:Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class)
    val decryptCipher: Cipher
        /**
         * Initializes and creates a decryption cipher
         * @return the cipher
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         */
        get() = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION)

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
        cipher: Cipher,
        secureRandom: SecureRandom
    ): ByteArray {
        val spec = getAlgorithmSpec(secureRandom)
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