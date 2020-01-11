import 'dart:convert';
import 'dart:math';
import 'dart:typed_data';


import 'package:basic_utils/basic_utils.dart';
import 'package:light_chat_client/transport/packetwrapper.dart';
import 'package:encrypt/encrypt.dart';
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
    var topLevel = ASN1Sequence();

    topLevel.add(ASN1Integer(key.modulus));
    topLevel.add(ASN1Integer(key.exponent));

    return base64Encode(topLevel.encodedBytes);
  }

  static KeyParameter generateSecretKeyParam() {
    return KeyParameter(createRandomBytes());
  }

  /// Code given by Richard Heap at https://stackoverflow.com/questions/59523956/how-to-encrypt-and-decrypt-using-aes-cbc-256bit-and-pkcs5padding-in-dart-and-als
  static EncryptedBytes encrypt(String data, KeyParameter keyParameter) {
    var key = keyParameter.key;
    var plainText = data;
    var random = Random.secure();
    var params = Uint8List(18)
      ..[0] = 4
      ..[1] = 16;
    for (var i = 2; i < 18; i++) {
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
    var plainBytes = utf8.encode(plainText);
    var cipherTextBytes = cipher.process(plainBytes);

    return EncryptedBytes(cipherTextBytes, params,
        'AES' /// Is hardcoded because this is what Java can recognize. This method might be modularized to use a Cipher parameter and a algorithm parameter that java recognizes later on
    );
  }

  /// Code given by Richard Heap at https://stackoverflow.com/questions/59523956/how-to-encrypt-and-decrypt-using-aes-cbc-256bit-and-pkcs5padding-in-dart-and-als
  static String decrypt(EncryptedBytes data, KeyParameter keyParameter) {
    var iv = data.params.sublist(2); // strip the 4, 16 DER header

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

    var plainishText = cipher.process(data.data);

    return utf8.decode(plainishText);
  }

  static Uint8List encryptSecretKey(RSAPublicKey key, Uint8List secretKey) {
    var encrypter = Encrypter(RSA(publicKey: key));

    return encrypter.encryptBytes(secretKey).bytes;
  }

  /// Code given by Richard Heap at https://stackoverflow.com/questions/59546025/how-to-import-randomly-generated-4096bit-java-rsa-public-key-from-string
  static RSAPublicKey rsaPublicKeyFromString(String key) {
    var pem =
    '-----BEGIN RSA PUBLIC KEY-----\n$key\n-----END RSA PUBLIC KEY-----';

    var public = X509Utils.publicKeyFromPem(pem);

    return public;
  }
}

class MyPadding extends PKCS7Padding {
  @override
  int padCount(Uint8List data) {
    print(data);
    return 0;
  }
}
