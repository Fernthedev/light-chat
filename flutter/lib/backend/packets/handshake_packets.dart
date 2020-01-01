import 'dart:typed_data';

import 'package:lightchat_client/backend/packets/packets.dart';
import 'package:lightchat_client/data/packetdata.dart';
import 'package:lightchat_client/util/encryption.dart';
import 'package:flutter/foundation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:lombok/lombok.dart';
import 'package:lightchat_client/backend/transport/packetwrapper.dart';
import 'package:pointycastle/export.dart';

part 'handshake_packets.g.dart';

@JsonSerializable(explicitToJson: true)
@ToString()
class InitialHandshakePacket extends Packet {
  static final InitialHandshakePacket constant = InitialHandshakePacket();

  String publicKey;
  VersionDataString versionData;

  get publicKeyAsKey {
    return EncryptionUtil.rsaPublicAsymmetricKeyStringToKey(publicKey);
  }

  @protected
  InitialHandshakePacket() : super.setName("INITIAL_HANDSHAKE_PACKET");

  factory InitialHandshakePacket.create(RSAPublicKey publicKey, VersionData versionData) {
    InitialHandshakePacket packet = InitialHandshakePacket();
    if(publicKey != null) packet.publicKey = EncryptionUtil.rsaAsymmetricKeyToString(publicKey);
    packet.versionData = versionData.toDataString();
    return packet;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory InitialHandshakePacket.fromJson(Map<String, dynamic> json) => _$InitialHandshakePacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return InitialHandshakePacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$InitialHandshakePacketToJson(this);
}

@JsonSerializable(explicitToJson: true)
@ToString()
class ConnectedPacket extends Packet {

  static final ConnectedPacket constant = ConnectedPacket();

  @protected
  ConnectedPacket() : super.setName("CONNECTED_PACKET");

  factory ConnectedPacket.create(String name, String os, VersionData versionData)  {
    ConnectedPacket connectedPacket = ConnectedPacket();
    connectedPacket.os = os;
    connectedPacket.name = name;
    connectedPacket.versionData = versionData.toDataString();
    return connectedPacket;
  }

  String name;
  String os;
  VersionDataString versionData;


  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory ConnectedPacket.fromJson(Map<String, dynamic> json) => _$ConnectedPacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return ConnectedPacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$ConnectedPacketToJson(this);
}

@JsonSerializable(explicitToJson: true, createFactory: false)
class KeyResponsePacket extends Packet {

  static final KeyResponsePacket constant = KeyResponsePacket();

  @protected
  KeyResponsePacket() : super.setName("KEY_RESPONSE_PACKET");

  factory KeyResponsePacket.create(Uint8List key, RSAPublicKey publicKey) {
    KeyResponsePacket keyPacket = KeyResponsePacket();
    keyPacket.secretKeyEncrypted = EncryptionUtil.encryptSecretKey(publicKey, key);
    return keyPacket;
  }

  Uint8List secretKeyEncrypted;

  KeyResponsePacket.fromJson(Map<String, dynamic> json) : secretKeyEncrypted = json['secretKeyEncrypted'];

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return KeyResponsePacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$KeyResponsePacketToJson(this);
}

@JsonSerializable()
@ToString()
class RequestConnectInfoPacket extends Packet {

  static final RequestConnectInfoPacket constant = RequestConnectInfoPacket();

  @protected
  RequestConnectInfoPacket() : super.setName("REQUEST_CONNECT_INFO_PACKET");

  factory RequestConnectInfoPacket.create() {
    RequestConnectInfoPacket pingPacket = RequestConnectInfoPacket();

    return pingPacket;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory RequestConnectInfoPacket.fromJson(Map<String, dynamic> json) => _$RequestConnectInfoPacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return RequestConnectInfoPacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$RequestConnectInfoPacketToJson(this);
}

