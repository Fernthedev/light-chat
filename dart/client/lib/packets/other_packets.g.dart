// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'other_packets.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CommandPacket _$CommandPacketFromJson(Map<String, dynamic> json) {
  return CommandPacket()..message = json['message'] as String;
}

Map<String, dynamic> _$CommandPacketToJson(CommandPacket instance) =>
    <String, dynamic>{
      'message': instance.message,
    };

IllegalConnection _$IllegalConnectionFromJson(Map<String, dynamic> json) {
  return IllegalConnection()..message = json['message'] as String;
}

Map<String, dynamic> _$IllegalConnectionToJson(IllegalConnection instance) =>
    <String, dynamic>{
      'message': instance.message,
    };

MessagePacket _$MessagePacketFromJson(Map<String, dynamic> json) {
  return MessagePacket()..message = json['message'] as String;
}

Map<String, dynamic> _$MessagePacketToJson(MessagePacket instance) =>
    <String, dynamic>{
      'message': instance.message,
    };

SelfMessagePacket _$SelfMessagePacketFromJson(Map<String, dynamic> json) {
  return SelfMessagePacket()
    ..messageType =
        _$enumDecodeNullable(_$MessageTypeEnumMap, json['messageType']);
}

Map<String, dynamic> _$SelfMessagePacketToJson(SelfMessagePacket instance) =>
    <String, dynamic>{
      'messageType': _$MessageTypeEnumMap[instance.messageType],
    };

T _$enumDecode<T>(
  Map<T, dynamic> enumValues,
  dynamic source, {
  T unknownValue,
}) {
  if (source == null) {
    throw ArgumentError('A value must be provided. Supported values: '
        '${enumValues.values.join(', ')}');
  }

  final value = enumValues.entries
      .singleWhere((e) => e.value == source, orElse: () => null)
      ?.key;

  if (value == null && unknownValue == null) {
    throw ArgumentError('`$source` is not one of the supported values: '
        '${enumValues.values.join(', ')}');
  }
  return value ?? unknownValue;
}

T _$enumDecodeNullable<T>(
  Map<T, dynamic> enumValues,
  dynamic source, {
  T unknownValue,
}) {
  if (source == null) {
    return null;
  }
  return _$enumDecode<T>(enumValues, source, unknownValue: unknownValue);
}

const _$MessageTypeEnumMap = {
  MessageType.FILL_PASSWORD: 'FILL_PASSWORD',
  MessageType.LOST_SERVER_CONNECTION: 'LOST_SERVER_CONNECTION',
  MessageType.REGISTER_PACKET: 'REGISTER_PACKET',
  MessageType.TIMED_OUT_REGISTRATION: 'TIMED_OUT_REGISTRATION',
};

HashedPasswordPacket _$HashedPasswordPacketFromJson(Map<String, dynamic> json) {
  return HashedPasswordPacket()
    ..hashedPassword = json['hashedPassword'] == null
        ? null
        : HashedPassword.fromJson(
            json['hashedPassword'] as Map<String, dynamic>);
}

Map<String, dynamic> _$HashedPasswordPacketToJson(
        HashedPasswordPacket instance) =>
    <String, dynamic>{
      'hashedPassword': instance.hashedPassword,
    };
