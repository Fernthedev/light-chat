package com.github.fernthedev.core.encryption.util;

import com.github.fernthedev.core.StaticHandler;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncryptionUtil {

    private RSAEncryptionUtil() {}


    /**
     * Encrypts the secret key with the public key
     * @param secretKey The encrypted key
     * @param publicKey The key for encrypting the encrypted key
     * @return Encrypted key
     * @throws InvalidKeyException The key is not valid
     */
    public static byte[] encryptKey(SecretKey secretKey, PublicKey publicKey) throws InvalidKeyException {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return rsaCipher.doFinal(secretKey.getEncoded());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypts the secret key with the public key
     * @param secretKey The encrypted key
     * @param privateKey The key for decrypting the encrypted key
     * @return Decrypted key key
     * @throws InvalidKeyException The key is not valid
     */
    public static SecretKey decryptKey(byte[] secretKey, PrivateKey privateKey) throws InvalidKeyException {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);

//            Cipher aesCipher = Cipher.getInstance("AES");
//            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedKey = rsaCipher.doFinal(secretKey);

            return new SecretKeySpec(decryptedKey,  StaticHandler.AES_KEY_MODE);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a key pair
     * @return The pair
     */
    public static KeyPair generateKeyPairs() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        keyGen.initialize(StaticHandler.KEY_SIZE);
        return keyGen.generateKeyPair();
    }



    public static PublicKey toPublicKey(String base64PublicKey){
        PublicKey publicKey;
        try{
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey toPrivateKey(String base64PrivateKey){
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static String toBase64(Key publicKey) {
        byte[] encodedPublicKey = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedPublicKey);
    }
}
