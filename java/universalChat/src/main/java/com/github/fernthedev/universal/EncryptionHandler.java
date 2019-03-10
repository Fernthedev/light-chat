package com.github.fernthedev.universal;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class EncryptionHandler {

    @Deprecated
    public static void logEncrypt(String password, Object encrypted) {
        System.out.println("Encrypting with password " + password + " the object \"" + encrypted + "\"");
    }

    @Deprecated
    public static void logDecrypt(String password, Object encrypted) {
        System.out.println("Decrypting with password " + password + " the object \"" + encrypted + "\"");
    }

    /**
     * Encrypt text with password
     * @param plainText Text to be encrypted
     * @param password Password to use for encryption
     * @return Encrypted version of plaintext
     */
    public static String encrypt(String plainText, String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());

            Cipher cipher = Cipher.getInstance(StaticHandler.getCipherTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] encryptedText = cipher.doFinal(plainText.getBytes("UTF-8"));

            // concatenate salt + iv + ciphertext
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encryptedText);

            // properly encode the complete ciphertext
           // logEncrypt(password, plainText);
            return DatatypeConverter.printBase64Binary(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Encrypt object with password
     * @param object Object to be encrypted
     * @param password Password to use for encryption
     * @return Encrypted version of object
     */
    public static SealedObject encrypt(Serializable object, String password) {
        try {
            byte[] salt = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());

            Cipher cipher = Cipher.getInstance(StaticHandler.getObjecrCipherTrans());
            cipher.init(Cipher.ENCRYPT_MODE, secret);

            // properly encode the complete ciphertext
            //logEncrypt(password, object);
            return new SealedObject(object, cipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt string encrypted using {@link this#encrypt(String, String)}
     * @param encryptedText Encrypted string
     * @param password Same password to decrypt
     * @return Plain text
     */
    public static String decrypt(String encryptedText, String password) {
        try {
            byte[] ciphertext = DatatypeConverter.parseBase64Binary(encryptedText);
            if (ciphertext.length < 48) {
                return null;
            }
            byte[] salt = Arrays.copyOfRange(ciphertext, 0, 16);
            byte[] iv = Arrays.copyOfRange(ciphertext, 16, 32);
            byte[] ct = Arrays.copyOfRange(ciphertext, 32, ciphertext.length);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());
            Cipher cipher = Cipher.getInstance(StaticHandler.getCipherTransformation());

            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] plaintext = cipher.doFinal(ct);

            //logDecrypt(password, encryptedText);
            return new String(plaintext, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt object using {@link this#encrypt(Serializable, String)}
     * @param sealedObject Encrypted object to decrypt
     * @param key Password to decrypt
     * @return Unencrypted object
     */
    public static Object decrypt(SealedObject sealedObject, String key) {
        try {
            byte[] salt = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());
            Cipher cipher = Cipher.getInstance(StaticHandler.getObjecrCipherTrans());

            cipher.init(Cipher.DECRYPT_MODE, secret);


           // logDecrypt(key, sealedObject);
            return sealedObject.getObject(cipher);
        } catch (BadPaddingException e) {
            try {
                throw new BadPaddingException("Password is: " + key);
            } catch (BadPaddingException e1) {
                e1.printStackTrace();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Hashes the string
     * @param input String to be hashed
     * @return The hashed string
     */
    public static String makeSHA256Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            byte[] buffer = input.getBytes("UTF-8");
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuilder hexStr = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                hexStr.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hexStr.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
