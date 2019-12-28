import 'dart:convert';
import 'dart:typed_data';

import 'package:pointycastle/asymmetric/api.dart';
import 'package:asn1lib/asn1lib.dart';

class EncryptionUtil {
  static String rsaAsymmetricKeyToString(RSAAsymmetricKey key) {
    var topLevel = new ASN1Sequence();

    topLevel.add(ASN1Integer(key.modulus));
    topLevel.add(ASN1Integer(key.exponent));

    return base64Encode(topLevel.encodedBytes);
  }

  static RSAAsymmetricKey rsaAsymmetricKeyStringToKey(String key) {

    String decoded64 = Utf8Decoder().convert(base64.decode(key)).trim();


//    var topLevel = new ASN1Sequence();
//
//    topLevel.add(ASN1Integer(key.modulus));
//    topLevel.add(ASN1Integer(key.exponent));
//
//    return base64.encode(topLevel.encodedBytes);
  }
}