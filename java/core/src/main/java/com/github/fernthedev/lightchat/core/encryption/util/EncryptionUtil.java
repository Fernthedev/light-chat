package com.github.fernthedev.lightchat.core.encryption.util;

import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.encryption.EncryptedBytes;
import lombok.NonNull;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class EncryptionUtil {

    public static SecretKey generateSecretKey() {

        KeyGenerator generator;
        try {

            generator = KeyGenerator.getInstance(StaticHandler.AES_KEY_MODE);
            generator.init(StaticHandler.AES_KEY_SIZE); // The AES key size in number of bits (256)

            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Encrypt object with password
     *
     * @param object Object to be encrypted
     * @param secret Password to use for encryption
     * @return Encrypted version of object
     */
    @Deprecated
    public static SealedObject encrypt(Serializable object, SecretKey secret) {
        try {
            Cipher cipher = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION);
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
     * Encrypt object with password
     *
     * @param data   Object to be encrypted
     * @param secret Password to use for encryption
     * @return Encrypted version of object
     */
    public static EncryptedBytes encrypt(String data, SecretKey secret) throws InvalidKeyException {

        try {
            Cipher cipher = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secret);

            // properly encode the complete ciphertext
            //logEncrypt(password, object);

            byte[] encodedData = cipher.doFinal(data.getBytes(StaticHandler.CHARSET_FOR_STRING));
            byte[] params = cipher.getParameters().getEncoded();
            String paramAlgorithm = cipher.getParameters().getAlgorithm();

            return new EncryptedBytes(encodedData, params, paramAlgorithm);
        } catch (InvalidKeyException e) {
            throw e;
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt data with secret
     *
     * @param encryptedBytes Object to be decrypted
     * @param secret         Password to use for decryption
     * @return Decrypted version of object
     */
    public static String decrypt(EncryptedBytes encryptedBytes, @NonNull SecretKey secret) throws InvalidKeyException {
        try {

            // get parameter object for password-based encryption
            AlgorithmParameters algParams = AlgorithmParameters.getInstance(encryptedBytes.getParamAlgorithm());

            // initialize with parameter encoding from above
            algParams.init(encryptedBytes.getParams());

            Cipher cipher = Cipher.getInstance(StaticHandler.AES_CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secret, algParams);

            return new String(cipher.doFinal(encryptedBytes.getData()), StaticHandler.CHARSET_FOR_STRING);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Encrypt text with password
     *
     * @param plainText Text to be encrypted
     * @param password  Password to use for encryption
     * @return Encrypted version of plaintext
     */
    public static String encryptWithPassword(String plainText, String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKEY_FACTORY_STRING());
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKEY_SPEC_TRANSFORMATION());

            Cipher cipher = Cipher.getInstance(StaticHandler.getCIPHER_TRANSFORMATION_OLD());
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] encryptedText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

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
     *
     * @param object   Object to be encrypted
     * @param password Password to use for encryption
     * @return Encrypted version of object
     */
    public static SealedObject encryptWithPassword(Serializable object, String password) {
        try {
            byte[] salt = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKEY_FACTORY_STRING());
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKEY_SPEC_TRANSFORMATION());

            return encrypt(object, secret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Decrypt string encrypted using {@link #encryptWithPassword(String, String)}
     *
     * @param encryptedText Encrypted string
     * @param password      Same password to decrypt
     * @return Plain text
     */
    public static String decryptWithPassword(String encryptedText, String password) {
        try {
            byte[] ciphertext = DatatypeConverter.parseBase64Binary(encryptedText);
            if (ciphertext.length < 48) {
                return null;
            }
            byte[] salt = Arrays.copyOfRange(ciphertext, 0, 16);
            byte[] iv = Arrays.copyOfRange(ciphertext, 16, 32);
            byte[] ct = Arrays.copyOfRange(ciphertext, 32, ciphertext.length);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKEY_FACTORY_STRING());
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKEY_SPEC_TRANSFORMATION());
            Cipher cipher = Cipher.getInstance(StaticHandler.getCIPHER_TRANSFORMATION_OLD());

            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] plaintext = cipher.doFinal(ct);

            //logDecrypt(password, encryptedText);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt object using {@link #encryptWithPassword(Serializable, String)}
     *
     * @param sealedObject Encrypted object to decrypt
     * @param key          Password to decrypt
     * @return Unencrypted object
     */
    public static Object decryptWithPassword(SealedObject sealedObject, String key) {
        try {
            byte[] salt = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKEY_FACTORY_STRING());
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKEY_SPEC_TRANSFORMATION());
            Cipher cipher = Cipher.getInstance(StaticHandler.getCIPHER_TRANSFORMATION_OLD());

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
     *
     * @param input String to be hashed
     * @return The hashed string
     */
    @NonNull
    public static String makeSHA256Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            byte[] buffer = input.getBytes(StandardCharsets.UTF_8);
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuilder hexStr = new StringBuilder();
            for (byte b : digest) {
                hexStr.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return hexStr.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Unable to create hash");
    }

    private static class DatatypeConverter {
        static String printBase64Binary(byte[] val) {
            return _printBase64Binary(val);
        }

        static String _printBase64Binary(byte[] input) {
            return _printBase64Binary(input, 0, input.length);
        }

        static String _printBase64Binary(byte[] input, int offset, int len) {
            char[] buf = new char[((len + 2) / 3) * 4];
            int ptr = _printBase64Binary(input, offset, len, buf, 0);
            assert ptr == buf.length;
            return new String(buf);
        }


        /**
         * Encodes a byte array into a char array by doing base64 encoding.
         * <p>
         * The caller must supply a big enough buffer.
         *
         * @return the value of {@code ptr+((len+2)/3)*4}, which is the new offset
         * in the output buffer where the further bytes should be placed.
         */
        static int _printBase64Binary(byte[] input, int offset, int len, char[] buf, int ptr) {
            // encode elements until only 1 or 2 elements are left to encode
            int remaining = len;
            int i;
            for (i = offset; remaining >= 3; remaining -= 3, i += 3) {
                buf[ptr++] = encode(input[i] >> 2);
                buf[ptr++] = encode(
                        ((input[i] & 0x3) << 4)
                                | ((input[i + 1] >> 4) & 0xF));
                buf[ptr++] = encode(
                        ((input[i + 1] & 0xF) << 2)
                                | ((input[i + 2] >> 6) & 0x3));
                buf[ptr++] = encode(input[i + 2] & 0x3F);
            }
            // encode when exactly 1 element (left) to encode
            if (remaining == 1) {
                buf[ptr++] = encode(input[i] >> 2);
                buf[ptr++] = encode(((input[i]) & 0x3) << 4);
                buf[ptr++] = '=';
                buf[ptr++] = '=';
            }
            // encode when exactly 2 elements (left) to encode
            if (remaining == 2) {
                buf[ptr++] = encode(input[i] >> 2);
                buf[ptr++] = encode(((input[i] & 0x3) << 4)
                        | ((input[i + 1] >> 4) & 0xF));
                buf[ptr++] = encode((input[i + 1] & 0xF) << 2);
                buf[ptr++] = '=';
            }
            return ptr;
        }

        static char encode(int i) {
            return encodeMap[i & 0x3F];
        }

        private static final char[] encodeMap = initEncodeMap();

        private static char[] initEncodeMap() {
            char[] map = new char[64];
            int i;
            for (i = 0; i < 26; i++) {
                map[i] = (char) ('A' + i);
            }
            for (i = 26; i < 52; i++) {
                map[i] = (char) ('a' + (i - 26));
            }
            for (i = 52; i < 62; i++) {
                map[i] = (char) ('0' + (i - 52));
            }
            map[62] = '+';
            map[63] = '/';

            return map;
        }

        static byte[] parseBase64Binary(String lexicalXSDBase64Binary) {
            return _parseBase64Binary(lexicalXSDBase64Binary);
        }


        /**
         * @param text base64Binary data is likely to be long, and decoding requires
         *             each character to be accessed twice (once for counting length, another
         *             for decoding.)
         *             <p>
         *             A benchmark showed that taking {@link String} is faster, presumably
         *             because JIT can inline a lot of string access (with data of 1K chars, it was twice as fast)
         */
        static byte[] _parseBase64Binary(String text) {
            final int buflen = guessLength(text);
            final byte[] out = new byte[buflen];
            int o = 0;

            final int len = text.length();
            int i;

            final byte[] quadruplet = new byte[4];
            int q = 0;

            // convert each quadruplet to three bytes.
            for (i = 0; i < len; i++) {
                char ch = text.charAt(i);
                byte v = decodeMap[ch];

                if (v != -1) {
                    quadruplet[q++] = v;
                }

                if (q == 4) {
                    // quadruplet is now filled.
                    out[o++] = (byte) ((quadruplet[0] << 2) | (quadruplet[1] >> 4));
                    if (quadruplet[2] != PADDING) {
                        out[o++] = (byte) ((quadruplet[1] << 4) | (quadruplet[2] >> 2));
                    }
                    if (quadruplet[3] != PADDING) {
                        out[o++] = (byte) ((quadruplet[2] << 6) | (quadruplet[3]));
                    }
                    q = 0;
                }
            }

            if (buflen == o) // speculation worked out to be OK
            {
                return out;
            }

            // we overestimated, so need to create a new buffer
            byte[] nb = new byte[o];
            System.arraycopy(out, 0, nb, 0, o);
            return nb;
        }

        /**
         * computes the length of binary data speculatively.
         *
         * <p>
         * Our requirement is to create byte[] of the exact length to store the binary data.
         * If we do this in a straight-forward way, it takes two passes over the data.
         * Experiments show that this is a non-trivial overhead (35% or so is spent on
         * the first pass in calculating the length.)
         *
         * <p>
         * So the approach here is that we compute the length speculatively, without looking
         * at the whole contents. The obtained speculative value is never less than the
         * actual length of the binary data, but it may be bigger. So if the speculation
         * goes wrong, we'll pay the cost of reallocation and buffer copying.
         *
         * <p>
         * If the base64 text is tightly packed with no indentation nor illegal char
         * (like what most web services produce), then the speculation of this method
         * will be correct, so we get the performance benefit.
         */
        private static int guessLength(String text) {
            final int len = text.length();

            // compute the tail '=' chars
            int j = len - 1;
            for (; j >= 0; j--) {
                byte code = decodeMap[text.charAt(j)];
                if (code == PADDING) {
                    continue;
                }
                if (code == -1) // most likely this base64 text is indented. go with the upper bound
                {
                    return text.length() / 4 * 3;
                }
                break;
            }

            j++;    // text.charAt(j) is now at some base64 char, so +1 to make it the size
            int padSize = len - j;
            if (padSize > 2) // something is wrong with base64. be safe and go with the upper bound
            {
                return text.length() / 4 * 3;
            }

            // so far this base64 looks like it's unindented tightly packed base64.
            // take a chance and create an array with the expected size
            return text.length() / 4 * 3 - padSize;
        }

        private static final byte[] decodeMap = initDecodeMap();

        private static final byte PADDING = 127;

        private static byte[] initDecodeMap() {
            byte[] map = new byte[128];
            int i;
            for (i = 0; i < 128; i++) {
                map[i] = -1;
            }

            for (i = 'A'; i <= 'Z'; i++) {
                map[i] = (byte) (i - 'A');
            }
            for (i = 'a'; i <= 'z'; i++) {
                map[i] = (byte) (i - 'a' + 26);
            }
            for (i = '0'; i <= '9'; i++) {
                map[i] = (byte) (i - '0' + 52);
            }
            map['+'] = 62;
            map['/'] = 63;
            map['='] = PADDING;

            return map;
        }
    }

}
