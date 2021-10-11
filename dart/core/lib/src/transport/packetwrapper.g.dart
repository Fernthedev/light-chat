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

Map<String, dynamic> _$ImplPacketWrapperToJson(ImplPacketWrapper instance) =>
    <String, dynamic>{
      'ENCRYPT': instance.encrypt,
      'jsonObject': instance.jsonObject,
      'packetIdentifier': instance.packetIdentifier,
    };

Map<String, dynamic> _$UnencryptedPacketWrapperToJson(
        UnencryptedPacketWrapper instance) =>
    <String, dynamic>{
      'ENCRYPT': instance.encrypt,
      'jsonObject': instance.jsonObject,
      'packetIdentifier': instance.packetIdentifier,
    };

Map<String, dynamic> _$EncryptedPacketWrapperToJson(
        EncryptedPacketWrapper instance) =>
    <String, dynamic>{
      'ENCRYPT': instance.encrypt,
      'jsonObject': instance.jsonObject,
      'packetIdentifier': instance.packetIdentifier,
    };

Map<String, dynamic> _$EncryptedBytesToJson(EncryptedBytes instance) =>
    <String, dynamic>{
      'data': instance.data,
      'params': instance.params,
      'paramAlgorithm': instance.paramAlgorithm,
    };
