import 'dart:convert';
import 'dart:core';
import 'dart:typed_data';

import 'package:lightchat_client/backend/packets/packets.dart';
import 'package:lightchat_client/backend/transport/packet_registry.dart';
import 'package:flutter/foundation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:lombok/lombok.dart';

import 'codecs/AcceptablePacketTypes.dart';

part 'packetwrapper.g.dart';

@JsonSerializable(explicitToJson:true, createFactory: false)
abstract class PacketWrapper<T> extends JsonSerializableClass {

  @JsonKey(name: "ENCRYPT")
  bool encrypt;

  @getter
  final dynamic jsonObject;

  @getter
  final String packetIdentifier;


  @override
  String toString() {
    return "PacketWrapper{" +
        ", aClass='" + packetIdentifier + '\'' +
        '}';
  }

  PacketWrapper(this.jsonObject, this.packetIdentifier) {
    encrypt = false;
  }

  static String getPacketIdentifier() {
    return "packetIdentifier";
  }

  static String getJsonIdentifier() {
    return "jsonObject";
  }
}

abstract class JsonSerializableClass {
  JsonSerializableClass fromJson(Map<String, dynamic> json);

  Map<String, dynamic> toJson();
}

/// Wraps a packet not meant to be encrypted
@JsonSerializable(explicitToJson: true, createFactory: false)
class UnencryptedPacketWrapper extends PacketWrapper<Map<String, dynamic>> implements AcceptablePacketTypes {

  static UnencryptedPacketWrapper _constant = UnencryptedPacketWrapper._self();
  UnencryptedPacketWrapper._self() : super(null, '');

  @protected
  _UnencryptedPacketWrapper() {}

  UnencryptedPacketWrapper(AcceptablePacketTypes jsonObject) : this.fromString(jsonObject.toJson(), jsonObject.getPacketName());

  UnencryptedPacketWrapper.fromString(Map<String, dynamic> json, String packetIdentifier) : super(json, packetIdentifier) {
    if (PacketRegistry.checkIfRegisteredIdentifier(packetIdentifier) == RegisteredReturnValues.NOT_IN_REGISTRY) {
      throw ("The packet trying to be wrapped is not registered. \"$packetIdentifier\"");
    }

    encrypt = false;
  }


  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  Map<String, dynamic> toJson() => _$UnencryptedPacketWrapperToJson(this);

  @override
  UnencryptedPacketWrapper fromJson(Map<String, dynamic> json) {
//    print("JSON Deocded of UnencryptedPacketWrapper = ${json[PacketWrapper.getPacketIdentifier()]}\n object: ${json[PacketWrapper.getJsonIdentifier()]}");
    return UnencryptedPacketWrapper.fromString(jsonDecode(json[PacketWrapper.getJsonIdentifier()]), json[PacketWrapper.getPacketIdentifier()]);
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory UnencryptedPacketWrapper.fromJson(Map<String, dynamic> json) => _constant.fromJson(json);

  @override
  String getPacketName() {
    return packetIdentifier;
  }
}


/// Wraps a packet that is to be encrypted
@JsonSerializable(explicitToJson:true, createFactory: false)
class EncryptedPacketWrapper extends PacketWrapper<EncryptedBytes> implements AcceptablePacketTypes {

  static EncryptedPacketWrapper _constant = EncryptedPacketWrapper._self();
  EncryptedPacketWrapper._self() : super(null, '');

  @protected
  _EncryptedPacketWrapper () {}



  EncryptedPacketWrapper (EncryptedBytes encryptedBytes, String packetIdentifier) : super(encryptedBytes, packetIdentifier) {
    if (PacketRegistry.checkIfRegisteredIdentifier(packetIdentifier) == RegisteredReturnValues.NOT_IN_REGISTRY) {
      throw ("The packet trying to be wrapped is not registered. \"$packetIdentifier\"");
    }

    encrypt = true;
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  Map<String, dynamic> toJson() => _$EncryptedPacketWrapperToJson(this);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return EncryptedPacketWrapper(EncryptedBytes.fromJson(json[PacketWrapper.getJsonIdentifier()]),
      json[PacketWrapper.getPacketIdentifier()]
    );
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory EncryptedPacketWrapper.fromJson(Map<String, dynamic> json) => _constant.fromJson(json);

  @override
  String getPacketName() {
    return packetIdentifier;
  }
}

@getter
@JsonSerializable(explicitToJson: true, createFactory: false)
class EncryptedBytes implements JsonSerializableClass {

  final Uint8List data;
  final Uint8List params;
  final String paramAlgorithm;

  EncryptedBytes(this.data, this.params, this.paramAlgorithm);

  EncryptedBytes.fromJson(Map<String, dynamic> json)
      : data = json['data'],
        params = json['params'],
        paramAlgorithm = json['paramAlgorithm'];


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