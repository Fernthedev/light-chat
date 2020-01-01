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

// **************************************************************************
// GetterGenerator
// **************************************************************************

abstract class _$EncryptedBytesLombok {
  /// Field
  Uint8List data;
  Uint8List params;
  String paramAlgorithm;

  /// Setter

  /// Getter
  Uint8List getData() {
    return data;
  }

  Uint8List getParams() {
    return params;
  }

  String getParamAlgorithm() {
    return paramAlgorithm;
  }
}
