// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'packetwrapper.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Map<String, dynamic> _$PacketWrapperToJson<T>(PacketWrapper<T> instance) =>
    <String, dynamic>{
      'ENCRYPT': instance.encrypt,
      'jsonObject': instance.jsonObject,
      'packetIdentifier': instance.packetIdentifier,
    };

UnencryptedPacketWrapper _$UnencryptedPacketWrapperFromJson(
    Map<String, dynamic> json) {
  return UnencryptedPacketWrapper(
    json['jsonObject'] == null
        ? null
        : Packet.fromJson(json['jsonObject'] as Map<String, dynamic>),
  )..encrypt = json['ENCRYPT'] as bool;
}

Map<String, dynamic> _$UnencryptedPacketWrapperToJson(
        UnencryptedPacketWrapper instance) =>
    <String, dynamic>{
      'ENCRYPT': instance.encrypt,
      'jsonObject': instance.jsonObject,
    };
