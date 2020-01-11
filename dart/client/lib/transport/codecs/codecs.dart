import 'dart:convert';

import 'dart:typed_data';

import 'package:light_chat_client/transport/packet_registry.dart';
import 'package:light_chat_client/util/encryption/encryption.dart';
import 'package:light_chat_client/variables.dart';
import 'package:pointycastle/api.dart';

import '../packetwrapper.dart';
import 'AcceptablePacketTypes.dart';

abstract class ObjectEncoder<T> {
  /// Encodes [msg] and adds to [out]
  void encode(T msg, List<Object> out);
}

abstract class ObjectDecoder<T> {
  /// Decodes [msg] and adds to [out]
  void decode(T msg, List<Object> out);
}

class StringEncoder extends ObjectEncoder<String> {
  StringEncoder([this.encoding = utf8]);

  final Encoding encoding;

  @override
  Future<void> encode(String msg, List<Object> out) async {
    out.add(encoding.encode(msg));
    return Future.value();
  }
}

/// Adds end of data identifier
class LineEndStringEncoder extends StringEncoder {
  final String endString;

  LineEndStringEncoder([Encoding encoding = utf8, this.endString = '\n\r'])
      : super(encoding);

  @override
  Future<void> encode(String msg, List<Object> out) async {
    await super.encode(msg + endString, out);
    return Future.value();
  }
}

/// Encodes encrypted data
class EncryptedJSONObjectEncoder extends ObjectEncoder<AcceptablePacketTypes> {
  final IKeyEncriptionHolder keyEncryptionHolder;
  final LineEndStringEncoder lineEndStringEncoder;

  EncryptedJSONObjectEncoder(
      this.keyEncryptionHolder, this.lineEndStringEncoder);

  @override
  Future<void> encode(AcceptablePacketTypes msg, List<Object> out) async {
    String sendJson;
    if (msg is UnencryptedPacketWrapper) {
      var decryptedJson = msg.toJson();

      decryptedJson[PacketWrapper.getJsonIdentifier()] = msg.jsonObject;

//      Map<String, dynamic> decryptedJSONOld = msg.toJson();
//
//      Map<String, dynamic> decryptedJson = Map();
//
//      decryptedJSONOld.forEach((f, v) {
//        decryptedJson[f.replaceAll("\"", "\\\"")] = v;
//      });

//      decryptedJson[PacketWrapper.getJsonIdentifier()] = jsonEncode(decryptedJson[PacketWrapper.getJsonIdentifier()]).replaceAll("\"", "\\\"");

      sendJson = jsonEncode(decryptedJson);
    } else {
      var decryptedJSON = msg.toJson();

//      Map<String, dynamic> decryptedJSONOld = msg.toJson();
//
//      Map<String, dynamic> decryptedJSON = Map();
//
//      decryptedJSONOld.forEach((f, v) {
//        decryptedJSON[f.replaceAll("\"", "\\\"").replaceAll("\\\\\"", "\\\"")] = v;
//      });

      var key = keyEncryptionHolder.getKey();
      var encryptedBytes =
          EncryptionUtil.encrypt(jsonEncode(decryptedJSON), key);

      var packetWrapper =
          EncryptedPacketWrapper(encryptedBytes, msg.getPacketName());
      var jsonPacketWrapper = packetWrapper.toJson();
//      jsonPacketWrapper[PacketWrapper.getJsonIdentifier()] = encryptedBytes;

//      jsonPacketWrapper[PacketWrapper.getJsonIdentifier()] = jsonEncode(jsonPacketWrapper[PacketWrapper.getJsonIdentifier()]).replaceAll("\"", "\\\"").replaceAll("\\\\\"", "\\\"");

      sendJson = jsonEncode(jsonPacketWrapper);
    }

    await lineEndStringEncoder.encode(sendJson, out);
    return Future.value();
  }
}

/// DECODE STRING AND ENCRYPTED STRING
class StringDecoder extends ObjectDecoder<Uint8List> {
  StringDecoder([this.encoding = utf8]);

  final Encoding encoding;

  @override
  Future<void> decode(Uint8List msg, List<Object> out) async {
    out.add(encoding.decode(msg));
    return Future.value();
  }
}

abstract class IKeyEncriptionHolder {
  bool isEncryptionKeyRegistered();

  KeyParameter getKey();
}

class EncryptedJSONObjectDecoder extends StringDecoder {
  final IKeyEncriptionHolder keyEncryptionHolder;
  EncryptedJSONObjectDecoder(this.keyEncryptionHolder,
      [Encoding encoding = utf8])
      : super(encoding);

  @override
  Future<void> decode(Uint8List msg, List<Object> out) async {
    var tempDecodeList = <Object>[];
    await super.decode(msg, tempDecodeList);

    var decodedString = '${tempDecodeList[0]}';
    Map<String, dynamic> jsonMapPacketWrapper;

    if (Variables.debug) print('Parsing: $decodedString');


    jsonMapPacketWrapper = json.decode(decodedString);

    PacketWrapper packetWrapper =
        ImplPacketWrapper.fromJson(jsonMapPacketWrapper);

    Map<String, dynamic> decryptedJsonObject;

    if (packetWrapper.encrypt) {
      packetWrapper = EncryptedPacketWrapper.fromJson(jsonMapPacketWrapper);

      var encryptedBytes =
          EncryptedBytes.fromJson(jsonDecode(packetWrapper.jsonObject));

      decryptedJsonObject = _decrypt(encryptedBytes);
    } else {
      packetWrapper = UnencryptedPacketWrapper.fromJson(jsonMapPacketWrapper);
      decryptedJsonObject = jsonDecode(packetWrapper.jsonObject);
    }

    if (Variables.debug) print('Decrypted json object: $decryptedJsonObject');

    out.add(
        getParsedObject(packetWrapper.packetIdentifier, decryptedJsonObject));

    return Future.value();
  }

  static dynamic getParsedObject(
      String packetIdentifier, Map<String, dynamic> jsonObject) {
    return PacketRegistry.getPacketInstanceFromRegistry(
        packetIdentifier, jsonObject);
  }

  Map<String, dynamic> _decrypt(EncryptedBytes jsonObject) {
    if (!keyEncryptionHolder.isEncryptionKeyRegistered()) {
      throw 'No Secret Key registered';
    }

    var secretKey = keyEncryptionHolder.getKey();

    if (secretKey == null) {
      throw 'No Secret Key registered';
    }

    var decryptedJSON = EncryptionUtil.decrypt(jsonObject, secretKey);

    return jsonDecode(decryptedJSON);
  }
}
