import 'dart:convert';
import 'dart:core';
import 'dart:typed_data';

import 'package:json_annotation/json_annotation.dart';

import 'codecs/AcceptablePacketTypes.dart';
import 'packet_registry.dart';

part 'packetwrapper.g.dart';

@JsonSerializable(explicitToJson: true, createFactory: false)
abstract class PacketWrapper<T> extends JsonSerializableClass {
  @JsonKey(name: 'ENCRYPT')
  bool encrypt = false;

  String? jsonObject;

  @JsonKey(ignore: true)
  final JsonSerializableClass? jsonObjectInstance;

  final String? packetIdentifier;

  @override
  String toString() {
    return ", aClass='" + packetIdentifier! + '\'' + '}' + 'PacketWrapper{';
  }

  PacketWrapper.empty([this.jsonObjectInstance, this.packetIdentifier]);

  PacketWrapper(this.jsonObjectInstance, this.packetIdentifier) {
    jsonObject = jsonEncode(jsonObjectInstance!.toJson());
    encrypt = false;
  }

  static String getPacketIdentifier() {
    return 'packetIdentifier';
  }

  static String getJsonIdentifier() {
    return 'jsonObject';
  }

  static String getEncryptIdentifier() {
    return 'ENCRYPT';
  }
}

abstract class JsonSerializableClass {
  JsonSerializableClass fromJson(Map<String, dynamic> json);

  Map<String, dynamic> toJson();
}

class JsonSerializableMap extends JsonSerializableClass {
  final Map<String, dynamic> json;

  JsonSerializableMap(this.json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return JsonSerializableMap(json);
  }

  @override
  Map<String, dynamic> toJson() {
    return json;
  }
}

/// Wraps a packet, but ignores it's data to check for encryption.
@JsonSerializable(explicitToJson: true, createFactory: false)
class ImplPacketWrapper extends PacketWrapper<dynamic>
    implements AcceptablePacketTypes {
  ImplPacketWrapper() : super.empty();

  static final ImplPacketWrapper _constant = ImplPacketWrapper._self();
  ImplPacketWrapper._self() : super.empty();

  ImplPacketWrapper.fromString(bool encrypt, String packetIdentifier)
      : super.empty(null, packetIdentifier) {
    if (PacketRegistry.checkIfRegisteredIdentifier(packetIdentifier) ==
        RegisteredReturnValues.NOT_IN_REGISTRY) {
      throw ('The packet trying to be wrapped is not registered. "$packetIdentifier"');
    }

    this.encrypt = encrypt;
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$ImplPacketWrapperToJson(this);

  @override
  ImplPacketWrapper fromJson(Map<String, dynamic> json) {
//    print("JSON Deocded of UnencryptedPacketWrapper = ${json[PacketWrapper.getPacketIdentifier()]}\n object: ${json[PacketWrapper.getJsonIdentifier()]}");
    return ImplPacketWrapper.fromString(
        json[PacketWrapper.getEncryptIdentifier()],
        json[PacketWrapper.getPacketIdentifier()]);
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory ImplPacketWrapper.fromJson(Map<String, dynamic> json) =>
      _constant.fromJson(json);

  @override
  String getPacketName() {
    return packetIdentifier!;
  }
}

/// Wraps a packet not meant to be encrypted
@JsonSerializable(explicitToJson: true, createFactory: false)
class UnencryptedPacketWrapper extends PacketWrapper<Map<String, dynamic>>
    implements AcceptablePacketTypes {
  static final UnencryptedPacketWrapper _constant =
      UnencryptedPacketWrapper._self();
  UnencryptedPacketWrapper._self() : super.empty();

  UnencryptedPacketWrapper(AcceptablePacketTypes jsonObject)
      : this.fromString(jsonObject.toJson(), jsonObject.getPacketName());

  UnencryptedPacketWrapper.fromString(
      Map<String, dynamic> json, String packetIdentifier)
      : super(JsonSerializableMap(json), packetIdentifier) {
    if (PacketRegistry.checkIfRegisteredIdentifier(packetIdentifier) ==
        RegisteredReturnValues.NOT_IN_REGISTRY) {
      throw ('The packet trying to be wrapped is not registered. "$packetIdentifier"');
    }

    encrypt = false;
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$UnencryptedPacketWrapperToJson(this);

  @override
  UnencryptedPacketWrapper fromJson(Map<String, dynamic> json) {
//    print("JSON Deocded of UnencryptedPacketWrapper = ${json[PacketWrapper.getPacketIdentifier()]}\n object: ${json[PacketWrapper.getJsonIdentifier()]}");
    return UnencryptedPacketWrapper.fromString(
        jsonDecode(json[PacketWrapper.getJsonIdentifier()]),
        json[PacketWrapper.getPacketIdentifier()]);
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory UnencryptedPacketWrapper.fromJson(Map<String, dynamic> json) =>
      _constant.fromJson(json);

  @override
  String getPacketName() {
    return packetIdentifier!;
  }
}

/// Wraps a packet that is to be encrypted
@JsonSerializable(explicitToJson: true, createFactory: false)
class EncryptedPacketWrapper extends PacketWrapper<EncryptedBytes>
    implements AcceptablePacketTypes {
  static final EncryptedPacketWrapper _constant =
      EncryptedPacketWrapper._self();
  EncryptedPacketWrapper._self() : super.empty(null, '');

  EncryptedPacketWrapper(EncryptedBytes encryptedBytes, String packetIdentifier)
      : super(encryptedBytes, packetIdentifier) {
    if (PacketRegistry.checkIfRegisteredIdentifier(packetIdentifier) ==
        RegisteredReturnValues.NOT_IN_REGISTRY) {
      throw ('The packet trying to be wrapped is not registered. "$packetIdentifier"');
    }

    encrypt = true;
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$EncryptedPacketWrapperToJson`.
  @override
  Map<String, dynamic> toJson() => _$EncryptedPacketWrapperToJson(this);

  @override
  EncryptedPacketWrapper fromJson(Map<String, dynamic> json) {
    return EncryptedPacketWrapper(
        EncryptedBytes.fromJson(
            jsonDecode(json[PacketWrapper.getJsonIdentifier()])),
        json[PacketWrapper.getPacketIdentifier()]);
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory EncryptedPacketWrapper.fromJson(Map<String, dynamic> json) =>
      _constant.fromJson(json);

  @override
  String getPacketName() {
    return packetIdentifier!;
  }
}

@JsonSerializable(explicitToJson: true, createFactory: false)
class EncryptedBytes implements JsonSerializableClass {
  Uint8List? data;
  Uint8List? params;
  final String? paramAlgorithm;

  EncryptedBytes(this.data, this.params, this.paramAlgorithm);

  EncryptedBytes.fromJson(Map<String, dynamic> json)
      : paramAlgorithm = json['paramAlgorithm'] {
    data = toUint8List(json['data']);
    params = toUint8List(json['params']);
  }

  Uint8List toUint8List(List<dynamic> list) {
    var intList = list
        .map((f) => f.runtimeType == String // f is a string
                ? int.tryParse(f) == null

                    /// If null, f is not a string
                    ? f as int // Cast as int
                    : int.parse(f) // if f is a string and correct number, parse
                : f as int // Cast as int
            )
        .toList(); // Return string as int

    return Uint8List.fromList(intList);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$EncryptedBytesToJson(this);

  @override
  EncryptedBytes fromJson(Map<String, dynamic> json) {
    return EncryptedBytes.fromJson(json);
  }
}
