import 'dart:typed_data';

import 'package:light_chat_client/data/packetdata.dart';
import 'package:light_chat_client/packets/packets.dart';
import 'package:light_chat_client/transport/packetwrapper.dart';
import 'package:light_chat_client/util/encryption/encryption.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:lombok/lombok.dart';
import 'package:pointycastle/export.dart';

part 'handshake_packets.g.dart';

@JsonSerializable(explicitToJson: true)
@ToString()
class InitialHandshakePacket extends Packet {
  static final InitialHandshakePacket constant = InitialHandshakePacket();

  String publicKey;
  VersionDataString versionData;

  RSAPublicKey get publicKeyAsKey => EncryptionUtil.rsaPublicKeyFromString(publicKey);

  InitialHandshakePacket() : super.setName('INITIAL_HANDSHAKE_PACKET');

  factory InitialHandshakePacket.create(RSAPublicKey publicKey, VersionData versionData) {
    var packet = InitialHandshakePacket();
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


  ConnectedPacket() : super.setName('CONNECTED_PACKET');

  factory ConnectedPacket.create(String name, String os, VersionData versionData)  {
    var connectedPacket = ConnectedPacket();
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

  KeyResponsePacket() : super.setName('KEY_RESPONSE_PACKET');

  factory KeyResponsePacket.create(Uint8List key, RSAPublicKey publicKey) {
    var keyPacket = KeyResponsePacket();
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


  RequestConnectInfoPacket() : super.setName('REQUEST_CONNECT_INFO_PACKET');

  factory RequestConnectInfoPacket.create() {
    var pingPacket = RequestConnectInfoPacket();

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

