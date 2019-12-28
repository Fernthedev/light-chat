
import 'dart:convert';

import 'package:chatclientflutter/backend/transport/codecs/AcceptablePacketTypes.dart';
import 'package:chatclientflutter/backend/transport/packet_registry.dart';
import 'package:chatclientflutter/util/encryption.dart';
import 'package:flutter/foundation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:lombok/lombok.dart';
import 'package:chatclientflutter/backend/transport/packetwrapper.dart';
import 'package:pointycastle/export.dart';


part 'packets.g.dart';

abstract class Packet implements AcceptablePacketTypes, JsonSerializableClass  {

  @protected
  Packet();

  String packetIdentifier;

  Packet.setName(this.packetIdentifier);



  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory Packet.fromJson(Map<String, dynamic> json) {
    return PacketRegistry.getPacketInstanceFromRegistry(json[PacketWrapper.getPacketIdentifier()], json[PacketWrapper.getJsonIdentifier()]);
  }

//  /// `toJson` is the convention for a class to declare support for serialization
//  /// to JSON. The implementation simply calls the private, generated
//  /// helper method `_$UserToJson`.
//  Map<String, dynamic> toJson() => _$PacketToJson(this);

}

@JsonSerializable()
@ToString()
class ConnectedPacket extends Packet {

  @protected
  ConnectedPacket();

  ConnectedPacket.setName() : super.setName("CONNECTED_PACKET");

  String name;

  String os;

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

@JsonSerializable()
@ToString()
class TemplatePacket extends Packet {

  @protected
  TemplatePacket();

  TemplatePacket.setName() : super.setName("TEMPLATE_PACKET");

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory TemplatePacket.fromJson(Map<String, dynamic> json) => _$TemplatePacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return TemplatePacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$TemplatePacketToJson(this);
}

@JsonSerializable()
@ToString()
class InitialHandshakePacket extends Packet {


  String publicKey;


  get publicKeyAsKey {

  }

  @protected
  InitialHandshakePacket();

  InitialHandshakePacket.create(RSAPublicKey publicKey) : super.setName("INITIAL_HANDSHAKE_PACKET") {
    this.publicKey = EncryptionUtil.rsaAsymmetricKeyToString(publicKey);
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