import 'dart:convert';
import 'dart:math';
import 'dart:typed_data';


import 'package:basic_utils/basic_utils.dart';
import 'package:encrypt/encrypt.dart';
import 'package:lightchat_client/backend/transport/packetwrapper.dart';
import 'package:pointycastle/api.dart';
import 'package:pointycastle/asymmetric/api.dart';

import 'package:asn1lib/asn1lib.dart';
import 'package:pointycastle/block/aes_fast.dart';
import 'package:pointycastle/block/modes/cbc.dart';
import 'package:pointycastle/export.dart';

class EncryptionUtil {
  static final Random _random = Random.secure();

  static Uint8List createRandomBytes([int length = 32]) {
    return Uint8List.fromList(List<int>.generate(length, (i) => _random.nextInt(256)));
  }

  static String createCryptoRandomString([int length = 32]) {
    return base64Url.encode(createRandomBytes(length));
  }

  static String rsaAsymmetricKeyToString(RSAAsymmetricKey key) {
    var topLevel = new ASN1Sequence();

    topLevel.add(ASN1Integer(key.modulus));
    topLevel.add(ASN1Integer(key.exponent));

    return base64Encode(topLevel.encodedBytes);
  }

  static KeyParameter generateSecretKeyParam() {
    return KeyParameter(createRandomBytes());
  }

  static generateAESEngine([KeyParameter keyParameter]) {
    if(keyParameter == null) keyParameter = generateSecretKeyParam();

    final AESFastEngine aes = AESFastEngine()..init(true, keyParameter); // true=encrypt

    return aes;
  }

  static EncryptedBytes encrypt(String data, KeyParameter keyParameter) {
//    final AESFastEngine aes = AESFastEngine()..init(true, keyParameter); // true=encrypt
//
//    Uint8List encryptedData = aes.process(utf8.encode(data)); // Needs to convert to UTF8 then Base64 and finally be encrypted
//    Uint8List params;
//
//    String algorithm = aes.algorithmName;
//
//    return EncryptedBytes(encryptedData, params, algorithm);

    var key = keyParameter.key;
    var plainText = data;
    var random = Random.secure();
    var params = Uint8List(18)
      ..[0] = 4
      ..[1] = 16;
    for (int i = 2; i < 18; i++) {
      params[i] = random.nextInt(256);
    }
    var iv = params.sublist(2);

    var cipher = PaddedBlockCipherImpl(
      PKCS7Padding(),
      CBCBlockCipher(AESFastEngine()),
    )..init(
      true /*encrypt*/,
      PaddedBlockCipherParameters<CipherParameters, CipherParameters>(
        ParametersWithIV<KeyParameter>(KeyParameter(key), iv),
        null,
      ),
    );

//    var plainBytes = utf8.encode(base64.encode(utf8.encode(plainText)));
//    var out = Uint8List(plainBytes.length + 16); // allow enough space
//    var length = cipher.doFinal(plainBytes, 0, out, 0);
//    Uint8List cipherText = out.sublist(0, length);
    Uint8List plainBytes = utf8.encode(plainText);
    Uint8List cipherTextBytes = cipher.process(plainBytes);

    return EncryptedBytes(cipherTextBytes, params, cipher.algorithmName);
  }

  static String decrypt(EncryptedBytes data, KeyParameter keyParameter) {
//    final AESFastEngine aes = AESFastEngine()..init(false, keyParameter); // false=decrypt

//    String encryptedData = utf8.decode(aes.process(data.data)); // Needs to be decrypted, then decoded from Base64 and finally UTF8
//    String params = utf8.decode(data.params);
    Uint8List iv = data.params.sublist(2); // strip the 4, 16 DER header

    var cipher = PaddedBlockCipherImpl(
      PKCS7Padding(),
      CBCBlockCipher(AESFastEngine()),
    );

    cipher.init(
      false /*decrypt*/,
      PaddedBlockCipherParameters<CipherParameters, CipherParameters>(
        ParametersWithIV<KeyParameter>(keyParameter, iv),
        null,
      ),
    );

    Uint8List plainishText = cipher.process(data.data);

    return utf8.decode(plainishText);
  }

  static Uint8List encryptSecretKey(RSAPublicKey key, Uint8List secretKey) {
    Encrypter encrypter = Encrypter(RSA(publicKey: key));

    return encrypter.encryptBytes(secretKey).bytes;
  }

  static RSAPublicKey rsaPublicAsymmetricKeyStringToKey(String key) {
    String pem =
    '-----BEGIN RSA PUBLIC KEY-----\n$key\n-----END RSA PUBLIC KEY-----';

    RSAPublicKey public = X509Utils.publicKeyFromPem(pem);

    return public;
//    String decoded64 = Utf8Decoder().convert(base64.decode(key)).trim();


//    var topLevel = new ASN1Sequence();
//
//    topLevel.add(ASN1Integer(key.modulus));
//    topLevel.add(ASN1Integer(key.exponent));
//
//    return base64.encode(topLevel.encodedBytes);
  }
}

class MyPadding extends PKCS7Padding {
  @override
  int padCount(Uint8List data) {
    print(data);
    return 0;
  }
}