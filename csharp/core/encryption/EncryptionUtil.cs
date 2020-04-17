using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Text;

namespace com.github.fernthedev.lightchat.core.encryption
{



    public class EncryptionUtil
    {

        public static readonly Aes aes = Aes.Create();


        public static EncryptedBytes encrypt(byte[] Key, byte[] IV, string plainText, Encoding? encoding)
        {
            if (encoding == null) encoding = StaticHandler.encoding;

            // Check arguments. 
            if (plainText == null || plainText.Length <= 0)
                throw new ArgumentNullException("plainText");
            if (Key == null || Key.Length <= 0)
                throw new ArgumentNullException("Key");
            if (IV == null || IV.Length <= 0)
                throw new ArgumentNullException("IV");


            byte[] encrypted;
            byte[] keyParams = IV;
            // Create an RijndaelManaged object 
            // with the specified key and IV. 
            using (RijndaelManaged rijAlg = new RijndaelManaged())
            {
                rijAlg.Key = Key;
                rijAlg.IV = IV;

                // Create a decryptor to perform the stream transform.
                ICryptoTransform encryptor = rijAlg.CreateEncryptor(rijAlg.Key, rijAlg.IV);



                // Create the streams used for encryption. 
                using (MemoryStream msEncrypt = new MemoryStream())
                {
                    using (CryptoStream csEncrypt = new CryptoStream(msEncrypt, encryptor, CryptoStreamMode.Write))
                    {
                        using (StreamWriter swEncrypt = new StreamWriter(csEncrypt))
                        {

                            //Write all data to the stream.
                            swEncrypt.Write(plainText);
                        }
                        encrypted = msEncrypt.ToArray();
                    }
                }
            }


            EncryptedBytes encryptedBytes = new EncryptedBytes(encrypted, keyParams, "AES");

            // Return the encrypted bytes from the memory stream. 
            return encryptedBytes;

            /* var buffer = encoding.GetBytes(str);

             byte[] result;

             var encryptor = aes.CreateEncryptor(aes.Key, aes.IV)
             using (var resultStream = new MemoryStream())
             {
                 using (var aesStream = new CryptoStream(resultStream, encryptor, CryptoStreamMode.Write))
                 using (var plainStream = new MemoryStream(buffer))
                 {
                     plainStream.CopyTo(aesStream);
                 }

                 result = resultStream.ToArray();
             }*/


        }

        public static string decrypt(EncryptedBytes encryptedBytes, byte[] Key, byte[] IV, Encoding? encoding)
        {
            if (encoding == null) encoding = StaticHandler.encoding;

            byte[] cipherText = encryptedBytes.data;

            // Check arguments. 
            if (cipherText == null || cipherText.Length <= 0)
                throw new ArgumentNullException("cipherText");
            if (Key == null || Key.Length <= 0)
                throw new ArgumentNullException("Key");
            if (IV == null || IV.Length <= 0)
                throw new ArgumentNullException("IV");

            // Declare the string used to hold 
            // the decrypted text. 
            string plaintext = null;

            // Create an RijndaelManaged object 
            // with the specified key and IV. 
            using (RijndaelManaged rijAlg = new RijndaelManaged())
            {
                rijAlg.Key = Key;
                rijAlg.IV = IV;

                // Create a decrytor to perform the stream transform.
                ICryptoTransform decryptor = rijAlg.CreateDecryptor(rijAlg.Key, rijAlg.IV);

                // Create the streams used for decryption. 
                using (MemoryStream msDecrypt = new MemoryStream(cipherText))
                {
                    using (CryptoStream csDecrypt = new CryptoStream(msDecrypt, decryptor, CryptoStreamMode.Read))
                    {
                        using (StreamReader srDecrypt = new StreamReader(csDecrypt))
                        {

                            // Read the decrypted bytes from the decrypting stream 
                            // and place them in a string.
                            plaintext = srDecrypt.ReadToEnd();
                        }
                    }
                }

            }

            return plaintext;

        }

        public static RijndaelManaged generateAESKey()
        {
            using (RijndaelManaged myRijndael = new RijndaelManaged())
            {

                myRijndael.Mode = CipherMode.CBC;

                myRijndael.GenerateKey();
                myRijndael.GenerateIV();

                return myRijndael;
            }
        }


        public static byte[] encryptKey(byte[] aesKey, RSAParameters publicKey)
        {
            using (var csp = new RSACryptoServiceProvider())
            {
                csp.ImportParameters(publicKey);

                return csp.Encrypt(aesKey, true);
            }
        }  
        
        public static byte[] decryptKey(byte[] encryptedAesKey, RSAParameters privateKey)
        {
            using (var csp = new RSACryptoServiceProvider())
            {
                csp.ImportParameters(privateKey);

                return csp.Decrypt(encryptedAesKey, true);
            }
        }


        private static byte[] GetRandomData(int bits)
        {
            var result = new byte[bits / 8];

            var gen = RandomNumberGenerator.Create();
            gen.GetBytes(result);

            gen.Dispose();

            return result;
        }
    }
}
