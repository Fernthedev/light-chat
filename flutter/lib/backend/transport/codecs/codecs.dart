import 'dart:convert';

import 'dart:typed_data';

import 'package:lightchat_client/backend/transport/codecs/AcceptablePacketTypes.dart';
import 'package:lightchat_client/backend/transport/packet_registry.dart';
import 'package:lightchat_client/backend/transport/packetwrapper.dart';
import 'package:lightchat_client/util/encryption.dart';
import 'package:pointycastle/api.dart';

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
  void encode(String msg, List<Object> out) {
    out.add(encoding.encode(msg));
  }
}

/// Adds end of data identifier
class LineEndStringEncoder extends StringEncoder {

  final String endString;

  LineEndStringEncoder([Encoding encoding = utf8, this.endString = "\n\r"]) : super(encoding);

  @override
  void encode(String msg, List<Object> out) {
    super.encode(msg + endString, out);
  }
}

/// Encodes encrypted data
class EncryptedJSONObjectEncoder extends ObjectEncoder<AcceptablePacketTypes> {

  final IKeyEncriptionHolder keyEncryptionHolder;
  final LineEndStringEncoder lineEndStringEncoder;

  EncryptedJSONObjectEncoder(this.keyEncryptionHolder, this.lineEndStringEncoder);

  @override
  void encode(AcceptablePacketTypes msg, List<Object> out) {
    String sendJson;
    if (msg is UnencryptedPacketWrapper) {
      Map<String, dynamic> decryptedJson = msg.toJson();

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

      Map<String, dynamic> decryptedJSON = msg.toJson();

//      Map<String, dynamic> decryptedJSONOld = msg.toJson();
//
//      Map<String, dynamic> decryptedJSON = Map();
//
////      decryptedJSONOld.forEach((f, v) {
////        decryptedJSON[f.replaceAll("\"", "\\\"").replaceAll("\\\\\"", "\\\"")] = v;
////      });

      KeyParameter key = keyEncryptionHolder.getKey();



      EncryptedBytes encryptedBytes = EncryptionUtil.encrypt(jsonEncode(decryptedJSON), key);

      PacketWrapper packetWrapper = new EncryptedPacketWrapper(encryptedBytes, msg.getPacketName());
      Map<String, dynamic> jsonPacketWrapper = packetWrapper.toJson();

//      jsonPacketWrapper[PacketWrapper.getJsonIdentifier()] = jsonEncode(jsonPacketWrapper[PacketWrapper.getJsonIdentifier()]).replaceAll("\"", "\\\"").replaceAll("\\\\\"", "\\\"");

      sendJson = jsonEncode(jsonPacketWrapper);
    }

    lineEndStringEncoder.encode(sendJson, out);
  }

}

/// DECODE STRING AND ENCRYPTED STRING
class StringDecoder extends ObjectDecoder<Uint8List> {

  StringDecoder([this.encoding = utf8]);

  final Encoding encoding;

  @override
  void decode(Uint8List msg, List<Object> out) {
    out.add(encoding.decode(msg));
  }
}

abstract class IKeyEncriptionHolder {
  bool isEncryptionKeyRegistered();

  KeyParameter getKey();
}

class EncryptedJSONObjectDecoder extends StringDecoder {

  final IKeyEncriptionHolder keyEncryptionHolder;
  EncryptedJSONObjectDecoder(this.keyEncryptionHolder, [Encoding encoding = utf8]) : super(encoding);


  @override
  void decode(Uint8List msg, List<Object> out) {
    List<Object> tempDecodeList = List();
    super.decode(msg, tempDecodeList);

    String decodedString = "${tempDecodeList[0]}";

    Map<String, dynamic> jsonMapPacketWrapper;
    try {
      print("Parsing: ${json.decode(decodedString)}");
      jsonMapPacketWrapper = json.decode(decodedString);
    } catch (e) {
      print("Failure to parse JSON. $decodedString");
      throw e;
    }

    PacketWrapper packetWrapper = UnencryptedPacketWrapper.fromJson(jsonMapPacketWrapper);

    Map<String, dynamic> decryptedJsonObject;

    if (packetWrapper.encrypt) {
      packetWrapper = EncryptedPacketWrapper.fromJson(jsonMapPacketWrapper);
      decryptedJsonObject = _decrypt(packetWrapper.jsonObject);
    } else {
      decryptedJsonObject = packetWrapper.jsonObject;
    }

    out.add(getParsedObject(packetWrapper.packetIdentifier, decryptedJsonObject));
  }


  static dynamic getParsedObject(String packetIdentifier, Map<String, dynamic> jsonObject) {
    return PacketRegistry.getPacketInstanceFromRegistry(packetIdentifier, jsonObject);
  }

  Map<String, dynamic> _decrypt(EncryptedBytes jsonObject) {
    if(!keyEncryptionHolder.isEncryptionKeyRegistered()) throw "No Secret Key registered";

    KeyParameter secretKey = keyEncryptionHolder.getKey();

    if (secretKey == null) {
      throw "No Secret Key registered";
    }

    String decryptedJSON = EncryptionUtil.decrypt(jsonObject, secretKey);

    return jsonDecode(decryptedJSON);
  }

}