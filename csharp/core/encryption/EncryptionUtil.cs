using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using com.github.fernthedev.lightchat.core.util;
using Org.BouncyCastle.Asn1;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Security;

namespace com.github.fernthedev.lightchat.core.encryption
{



    public class EncryptionUtil
    {
//
//         public static EncryptedBytes encrypt(byte[] Key, byte[] IV, string plainText, Encoding? encoding)
//         {
//             if (encoding == null) encoding = StaticHandler.encoding;
//
//             // Check arguments.
//             if (plainText == null || plainText.Length <= 0)
//                 throw new ArgumentNullException("plainText");
//             if (Key == null || Key.Length <= 0)
//                 throw new ArgumentNullException("Key");
//             if (IV == null || IV.Length <= 0)
//                 throw new ArgumentNullException("IV");
//
//
//             byte[] encrypted;
//             byte[] keyParams = IV;
//             // Create an RijndaelManaged object
//             // with the specified key and IV.
//             using (RijndaelManaged rijAlg = new RijndaelManaged())
//             {
//                 rijAlg.Key = Key;
//                 rijAlg.IV = IV;
//
//                 // Create a decryptor to perform the stream transform.
//                 ICryptoTransform encryptor = rijAlg.CreateEncryptor(rijAlg.Key, rijAlg.IV);
//
//
//
//                 // Create the streams used for encryption.
//                 using (MemoryStream msEncrypt = new MemoryStream())
//                 {
//                     using (CryptoStream csEncrypt = new CryptoStream(msEncrypt, encryptor, CryptoStreamMode.Write))
//                     {
//                         using (StreamWriter swEncrypt = new StreamWriter(csEncrypt))
//                         {
//
//                             //Write all data to the stream.
//                             swEncrypt.Write(plainText);
//                         }
//                         encrypted = msEncrypt.ToArray();
//                     }
//                 }
//             }
//
//
//             EncryptedBytes encryptedBytes = new EncryptedBytes(encrypted, keyParams, "AES");
//
//             // Return the encrypted bytes from the memory stream.
//             return encryptedBytes;
//
//             /* var buffer = encoding.GetBytes(str);
//
//              byte[] result;
//
//              var encryptor = aes.CreateEncryptor(aes.Key, aes.IV)
//              using (var resultStream = new MemoryStream())
//              {
//                  using (var aesStream = new CryptoStream(resultStream, encryptor, CryptoStreamMode.Write))
//                  using (var plainStream = new MemoryStream(buffer))
//                  {
//                      plainStream.CopyTo(aesStream);
//                  }
//
//                  result = resultStream.ToArray();
//              }*/
//
//
//         }

        public static EncryptedBytes encrypt(AesCryptoServiceProvider aesCryptoServiceProvider, string plainText,
            Encoding encoding)
        {
            if (encoding == null) encoding = StaticHandler.encoding;

            // Check arguments.
            if (plainText == null || plainText.Length <= 0)
                throw new ArgumentNullException("plainText");
            if (aesCryptoServiceProvider.Key == null || aesCryptoServiceProvider.Key.Length <= 0)
                throw new ArgumentNullException("Key");
            if (aesCryptoServiceProvider.IV == null || aesCryptoServiceProvider.IV.Length <= 0)
                throw new ArgumentNullException("IV");


            ICryptoTransform transform = aesCryptoServiceProvider.CreateEncryptor();

            var encodedText = encoding.GetBytes(plainText);
            var encryptedText =
                transform.TransformFinalBlock(encodedText, 0, encodedText.Length).Select(Convert.ToSByte);

            var derOctetString = new DerOctetString(aesCryptoServiceProvider.IV);


            return new EncryptedBytes(encryptedText, derOctetString.GetEncoded().Select(Convert.ToSByte), "AES");
        }


        // public static string decrypt(EncryptedBytes encryptedBytes, byte[] Key, byte[] IV, Encoding? encoding)
        // {
        //     if (encoding == null) encoding = StaticHandler.encoding;
        //
        //     byte[] cipherText = encryptedBytes.data.ToArray();
        //
        //     // Check arguments.
        //     if (cipherText == null || cipherText.Length <= 0)
        //         throw new ArgumentNullException("cipherText");
        //     if (Key == null || Key.Length <= 0)
        //         throw new ArgumentNullException("Key");
        //     if (IV == null || IV.Length <= 0)
        //         throw new ArgumentNullException("IV");
        //
        //     // Declare the string used to hold
        //     // the decrypted text.
        //     string plaintext = null;
        //
        //     // Create an RijndaelManaged object
        //     // with the specified key and IV.
        //     using (RijndaelManaged rijAlg = new RijndaelManaged())
        //     {
        //         rijAlg.Key = Key;
        //         rijAlg.IV = IV;
        //
        //         // Create a decrytor to perform the stream transform.
        //         ICryptoTransform decryptor = rijAlg.CreateDecryptor(rijAlg.Key, rijAlg.IV);
        //
        //         // Create the streams used for decryption.
        //         using (MemoryStream msDecrypt = new MemoryStream(cipherText))
        //         {
        //             using (CryptoStream csDecrypt = new CryptoStream(msDecrypt, decryptor, CryptoStreamMode.Read))
        //             {
        //                 using (StreamReader srDecrypt = new StreamReader(csDecrypt))
        //                 {
        //
        //                     // Read the decrypted bytes from the decrypting stream
        //                     // and place them in a string.
        //                     plaintext = srDecrypt.ReadToEnd();
        //                 }
        //             }
        //         }
        //
        //     }
        //
        //     return plaintext;
        //
        // }

        public static string decrypt([NotNull] EncryptedBytes encryptedBytes, AesCryptoServiceProvider aesCryptoServiceProvider, Encoding? encoding)
        {
            if (encoding == null) encoding = StaticHandler.encoding;

            var cipherText = encryptedBytes.data.Select(arg => (byte) arg).ToArray();

            // Check arguments.
            if (cipherText == null || cipherText.Length <= 0)
                throw new ArgumentNullException("cipherText");
            if (aesCryptoServiceProvider.Key == null || aesCryptoServiceProvider.Key.Length <= 0)
                throw new ArgumentNullException("Key");
            if (aesCryptoServiceProvider.IV == null || aesCryptoServiceProvider.IV.Length <= 0)
                throw new ArgumentNullException("IV");

            // Declare the string used to hold
            // the decrypted text.

            aesCryptoServiceProvider.Padding = PaddingMode.PKCS7;

            var transform = aesCryptoServiceProvider.CreateDecryptor();
            string plaintext;

            // Create the streams used for decryption.
            // using (var msDecrypt = new MemoryStream(cipherText))
            // {
            //     using (var csDecrypt = new CryptoStream(msDecrypt, transform, CryptoStreamMode.Read))
            //     {
            //         using (var srDecrypt = new StreamReader(csDecrypt))
            //         {
            //
            //             // Read the decrypted bytes from the decrypting stream
            //             // and place them in a string.
            //             plaintext = srDecrypt.ReadToEnd();
            //         }
            //     }
            // }

            var keyParams = encryptedBytes.keyParams.Select(b => (byte) b).ToArray();

            var octetString = (DerOctetString) Asn1Object.FromByteArray(keyParams);

            aesCryptoServiceProvider.IV = octetString.GetOctets();


            // TODO: Remove this log
            StaticHandler.Core.Logger.Debug("Key, IV (no encode) and data: {0} {1} ({2}) {3} ({4})",
                Convert.ToBase64String(aesCryptoServiceProvider.Key),
                Convert.ToBase64String(keyParams),
                Convert.ToBase64String(octetString.GetOctets()),
                string.Join(',', cipherText),
                Convert.ToBase64String(cipherText)
            );

            plaintext = encoding.GetString(transform.TransformFinalBlock(cipherText, 0, cipherText.Length));

            return plaintext;

        }

        public static AesCryptoServiceProvider generateAESProvider()
        {

            var aesCryptoServiceProvider = new AesCryptoServiceProvider
            {
                KeySize = 256, Mode = CipherMode.CBC, Padding = PaddingMode.PKCS7
            };


            aesCryptoServiceProvider.GenerateIV();

            aesCryptoServiceProvider.GenerateKey();

            return aesCryptoServiceProvider;

        }
    }
}
