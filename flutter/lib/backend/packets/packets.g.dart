// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'packets.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ConnectedPacket _$ConnectedPacketFromJson(Map<String, dynamic> json) {
  return ConnectedPacket()
    ..packetIdentifier = json['packetIdentifier'] as String
    ..name = json['name'] as String
    ..os = json['os'] as String;
}

Map<String, dynamic> _$ConnectedPacketToJson(ConnectedPacket instance) =>
    <String, dynamic>{
      'packetIdentifier': instance.packetIdentifier,
      'name': instance.name,
      'os': instance.os,
    };

TemplatePacket _$TemplatePacketFromJson(Map<String, dynamic> json) {
  return TemplatePacket()
    ..packetIdentifier = json['packetIdentifier'] as String;
}

Map<String, dynamic> _$TemplatePacketToJson(TemplatePacket instance) =>
    <String, dynamic>{
      'packetIdentifier': instance.packetIdentifier,
    };

InitialHandshakePacket _$InitialHandshakePacketFromJson(
    Map<String, dynamic> json) {
  return InitialHandshakePacket()
    ..packetIdentifier = json['packetIdentifier'] as String
    ..publicKey = json['publicKey'] as String;
}

Map<String, dynamic> _$InitialHandshakePacketToJson(
        InitialHandshakePacket instance) =>
    <String, dynamic>{
      'packetIdentifier': instance.packetIdentifier,
      'publicKey': instance.publicKey,
    };
