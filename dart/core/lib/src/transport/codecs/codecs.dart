import 'dart:convert';

import 'dart:typed_data';

import '../../data/handlers.dart';

import '../../util/encryption/encryption.dart';
import '../../variables.dart';
import '../packet_registry.dart';
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
//
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

      var jsonString = jsonEncode(decryptedJSON);

      var key = keyEncryptionHolder.getKey();
      var encryptedBytes = EncryptionUtil.encrypt(jsonString, key);

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

@deprecated
class LineBasedFrameDecoder extends ObjectDecoder<Uint8List> {
  LineBasedFrameDecoder([this.encoding = utf8, this.seperator = '\n\r']);

  final Encoding encoding;
  final String seperator;

  Uint8List get seperatorByte => encoding.encode(seperator);

  @override
  void decode(Uint8List msg, List<Object> out) {
    var checkedIndex = 0;

    while (checkedIndex < msg.length) {
      var indexOfSeperate = msg.firstWhere((e) {
        if (e == seperatorByte.first) {
          var index = 1;

          if (seperatorByte.length <= index) return true;

          while (msg.length > e + index &&
              msg.elementAt(index) == msg.elementAt(e + index)) {
            if (seperatorByte.length <= index) return true;

            index++;
          }
        }

        return false;
      }, orElse: () {
        return -1;
      });

      if (indexOfSeperate == -1) {
        out.add(msg);
        return;
      }

      out.add(msg.sublist(checkedIndex, msg.indexOf(indexOfSeperate) - 1));
      checkedIndex = indexOfSeperate + seperatorByte.length;
    }
  }
}

class LineStringSeperatorDecoder extends StringDecoder {
  final String seperator;

  LineStringSeperatorDecoder(this.seperator, [Encoding encoding])
      : super(encoding);

  @override
  Future<void> decode(Uint8List msg, List<Object> out) async {
    var decoded = encoding.decode(msg);

    var msgSplit = decoded.split(seperator);

    for (var s in msgSplit) {
      var noSkipS = s.replaceAll(seperator, '');

      if (noSkipS == '') continue;

      out.add(noSkipS);
    }

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

class EncryptedJSONObjectDecoder extends LineStringSeperatorDecoder {
  final IKeyEncriptionHolder keyEncryptionHolder;
  EncryptedJSONObjectDecoder(this.keyEncryptionHolder,
      [Encoding encoding = utf8])
      : super('\n\r', encoding);

  @override
  Future<void> decode(Uint8List msg, List<Object> out) async {
    var seperatedDecodeList = <Object>[];
    // await lineBasedFrameDecoder.decode(msg, tempDecodeList);
//
    // for (Uint8List bytes in tempDecodeList) {
    // await super.decode(bytes, seperatedDecodeList);
    // }

    await super.decode(msg, seperatedDecodeList);

    for (var decodedString in seperatedDecodeList) {
      Map<String, dynamic> jsonMapPacketWrapper;

      PacketWrapper packetWrapper;

      // await Future.sync(() {
      jsonMapPacketWrapper = json.decode(decodedString);
      packetWrapper = ImplPacketWrapper.fromJson(jsonMapPacketWrapper);

      Map<String, dynamic> decryptedJsonObject;
      if (packetWrapper.encrypt) {
        // print("Unwrapping {$decodedString} because encrypt");

        packetWrapper = EncryptedPacketWrapper.fromJson(jsonMapPacketWrapper);

        var encryptedBytes;

        encryptedBytes =
            EncryptedBytes.fromJson(jsonDecode(packetWrapper.jsonObject));

        decryptedJsonObject = _decrypt(encryptedBytes);
      } else {
        packetWrapper = UnencryptedPacketWrapper.fromJson(jsonMapPacketWrapper);
        decryptedJsonObject = jsonDecode(packetWrapper.jsonObject);
      }

      //   if (Variables.debug) print(
      //      'Decrypted json object: $decryptedJsonObject');

      if (Variables.debug) {
        print(
            'Parsing: ${packetWrapper.packetIdentifier}  $decryptedJsonObject');
      }

      out.add(
          getParsedObject(packetWrapper.packetIdentifier, decryptedJsonObject));
    }
    // }).catchError((e) {
    //   Variables.printDebug("Parsed $decodedString");
    //   throw e;
    // });

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
