package com.github.fernthedev.lightchat.core.encryption.util

import com.github.fernthedev.lightchat.core.StaticHandler
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

object RSAEncryptionUtil {
    /**
     * Encrypts the secret key with the public key
     * @param secretKey The encrypted key
     * @param publicKey The key for encrypting the encrypted key
     * @return Encrypted key
     * @throws InvalidKeyException The key is not valid
     */
    @Throws(InvalidKeyException::class)
    fun encryptKey(secretKey: SecretKey, publicKey: PublicKey?): ByteArray? {
        try {
            val rsaCipher = Cipher.getInstance(StaticHandler.RSA_CIPHER_TRANSFORMATION)
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return rsaCipher.doFinal(secretKey.encoded)
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Decrypts the secret key with the public key
     * @param secretKey The encrypted key
     * @param privateKey The key for decrypting the encrypted key
     * @return Decrypted key key
     * @throws InvalidKeyException The key is not valid
     */
    @Throws(InvalidKeyException::class, BadPaddingException::class, NoSuchAlgorithmException::class)
    fun decryptKey(secretKey: ByteArray?, privateKey: PrivateKey): SecretKey {

        val rsaCipher = Cipher.getInstance(StaticHandler.RSA_CIPHER_TRANSFORMATION)
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)

//            Cipher aesCipher = Cipher.getInstance("AES");
//            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
        require(!(secretKey == null || secretKey.isEmpty())) {
            "Secret key is null or empty: " + Arrays.toString(
                secretKey
            )
        }
        val decryptedKey = rsaCipher.doFinal(secretKey)
        return SecretKeySpec(decryptedKey, StaticHandler.AES_KEY_MODE)

    }

    /**
     * Create a key pair
     * @return The pair
     */
    fun generateKeyPairs(keySize: Int): KeyPair {
        val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")

        keyGen.initialize(keySize)
        return keyGen.generateKeyPair()
    }

    fun toPublicKey(base64PublicKey: String?): PublicKey? {
        val publicKey: PublicKey
        try {
            val keySpec = X509EncodedKeySpec(
                Base64.getDecoder().decode(
                    base64PublicKey!!.toByteArray()
                )
            )
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(keySpec)
            return publicKey
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return null
    }

    fun toPrivateKey(base64PrivateKey: String): PrivateKey? {
        var privateKey: PrivateKey? = null
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.toByteArray()))
        val keyFactory: KeyFactory
        keyFactory = try {
            KeyFactory.getInstance("RSA")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec)
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return privateKey
    }

    fun toBase64(publicKey: Key): String {
        val encodedPublicKey = publicKey.encoded
        return Base64.getEncoder().encodeToString(encodedPublicKey)
    }
}