// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'handshake_packets.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InitialHandshakePacket _$InitialHandshakePacketFromJson(
    Map<String, dynamic> json) {
  return InitialHandshakePacket()
    ..publicKey = json['publicKey'] as String
    ..versionData = json['versionData'] == null
        ? null
        : VersionDataString.fromJson(
            json['versionData'] as Map<String, dynamic>);
}

Map<String, dynamic> _$InitialHandshakePacketToJson(
        InitialHandshakePacket instance) =>
    <String, dynamic>{
      'publicKey': instance.publicKey,
      'versionData': instance.versionData?.toJson(),
    };

ConnectedPacket _$ConnectedPacketFromJson(Map<String, dynamic> json) {
  return ConnectedPacket()
    ..name = json['name'] as String
    ..os = json['os'] as String
    ..versionData = json['versionData'] == null
        ? null
        : VersionDataString.fromJson(
            json['versionData'] as Map<String, dynamic>);
}

Map<String, dynamic> _$ConnectedPacketToJson(ConnectedPacket instance) =>
    <String, dynamic>{
      'name': instance.name,
      'os': instance.os,
      'versionData': instance.versionData?.toJson(),
    };

Map<String, dynamic> _$KeyResponsePacketToJson(KeyResponsePacket instance) =>
    <String, dynamic>{
      'secretKeyEncrypted': instance.secretKeyEncrypted,
    };

RequestConnectInfoPacket _$RequestConnectInfoPacketFromJson(
    Map<String, dynamic> json) {
  return RequestConnectInfoPacket();
}

Map<String, dynamic> _$RequestConnectInfoPacketToJson(
        RequestConnectInfoPacket instance) =>
    <String, dynamic>{};
