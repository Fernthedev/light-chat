import 'dart:convert';

import 'dart:typed_data';

import 'package:light_chat_core/packets.dart';

import '../../data/handlers.dart';

import '../../util/encryption/encryption.dart';
import '../../variables.dart';
import '../packet_registry.dart';
import '../packetwrapper.dart';
import 'AcceptablePacketTypes.dart';

abstract class ObjectEncoder<T> {
  /// Encodes [msg] and adds to [out]
  Future<void> encode(T msg, List<Object> out);
}

abstract class ObjectDecoder<T> {
  /// Decodes [msg] and adds to [out]
  Future<void> decode(T msg, List<Object> out);
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
    return await super.encode(msg + endString, out);
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

      sendJson = jsonEncode(decryptedJson);
    } else {
      var decryptedJSON = msg.toJson();

      var jsonString = jsonEncode(decryptedJSON);

      var key = keyEncryptionHolder.getKey()!;
      var encryptedBytes = EncryptionUtil.encrypt(jsonString, key);

      var packetWrapper =
          EncryptedPacketWrapper(encryptedBytes, msg.getPacketName());
      var jsonPacketWrapper = packetWrapper.toJson();

      sendJson = jsonEncode(jsonPacketWrapper);
    }

    return await lineEndStringEncoder.encode(sendJson, out);
  }
}

@deprecated
class LineBasedFrameDecoder extends ObjectDecoder<Uint8List> {
  LineBasedFrameDecoder([this.encoding = utf8, this.seperator = '\n\r']);

  final Encoding encoding;
  final String seperator;

  Uint8List get seperatorByte => Uint8List.fromList(encoding.encode(seperator));

  @override
  Future<void> decode(Uint8List msg, List<Object> out) async {
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

    return Future.value();
  }
}

class LineStringSeperatorDecoder extends StringDecoder {
  final String seperator;

  LineStringSeperatorDecoder(this.seperator, [Encoding encoding = utf8])
      : super(encoding);

  @override
  Future<void> decode(Uint8List msg, List<Object> out) async {
    var decoded = encoding.decode(msg);

    var msgSplit = decoded.split(seperator);

    for (var s in msgSplit) {
      var noSkipS = s.replaceAll(seperator, '');

      if (noSkipS.isEmpty) continue;

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

    await super.decode(msg, seperatedDecodeList);

    return Future.forEach(seperatedDecodeList, (decodedString) {
      if (decodedString is String) {
        Map<String, dynamic> jsonMapPacketWrapper;

        // await Future.sync(() {
        jsonMapPacketWrapper = json.decode(decodedString);
        PacketWrapper packetWrapper =
            ImplPacketWrapper.fromJson(jsonMapPacketWrapper);

        Map<String, dynamic> decryptedJsonObject;
        if (packetWrapper.encrypt) {
          // print("Unwrapping {$decodedString} because encrypt");

          packetWrapper = EncryptedPacketWrapper.fromJson(jsonMapPacketWrapper);

          EncryptedBytes encryptedBytes =
              EncryptedBytes.fromJson(jsonDecode(packetWrapper.jsonObject!));

          decryptedJsonObject = _decrypt(encryptedBytes);
        } else {
          packetWrapper =
              UnencryptedPacketWrapper.fromJson(jsonMapPacketWrapper);
          decryptedJsonObject = jsonDecode(packetWrapper.jsonObject!);
        }

        //   if (Variables.debug) print(
        //      'Decrypted json object: $decryptedJsonObject');

        if (Variables.debug) {
          print(
              'Parsing: ${packetWrapper.packetIdentifier}  $decryptedJsonObject');
        }

        out.add(getParsedObject(
            packetWrapper.packetIdentifier!, decryptedJsonObject));
      }
    });
  }

  static Packet getParsedObject(
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
