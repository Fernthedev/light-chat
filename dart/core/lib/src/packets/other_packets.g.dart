// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'other_packets.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CommandPacket _$CommandPacketFromJson(Map<String, dynamic> json) =>
    CommandPacket()..message = json['message'] as String;

Map<String, dynamic> _$CommandPacketToJson(CommandPacket instance) =>
    <String, dynamic>{
      'message': instance.message,
    };

IllegalConnection _$IllegalConnectionFromJson(Map<String, dynamic> json) =>
    IllegalConnection()..message = json['message'] as String;

Map<String, dynamic> _$IllegalConnectionToJson(IllegalConnection instance) =>
    <String, dynamic>{
      'message': instance.message,
    };

MessagePacket _$MessagePacketFromJson(Map<String, dynamic> json) =>
    MessagePacket()..message = json['message'] as String;

Map<String, dynamic> _$MessagePacketToJson(MessagePacket instance) =>
    <String, dynamic>{
      'message': instance.message,
    };

SelfMessagePacket _$SelfMessagePacketFromJson(Map<String, dynamic> json) =>
    SelfMessagePacket()
      ..type = _$enumDecode(_$MessageTypeEnumMap, json['type']);

Map<String, dynamic> _$SelfMessagePacketToJson(SelfMessagePacket instance) =>
    <String, dynamic>{
      'type': _$MessageTypeEnumMap[instance.type],
    };

K _$enumDecode<K, V>(
  Map<K, V> enumValues,
  Object? source, {
  K? unknownValue,
}) {
  if (source == null) {
    throw ArgumentError(
      'A value must be provided. Supported values: '
      '${enumValues.values.join(', ')}',
    );
  }

  return enumValues.entries.singleWhere(
    (e) => e.value == source,
    orElse: () {
      if (unknownValue == null) {
        throw ArgumentError(
          '`$source` is not one of the supported values: '
          '${enumValues.values.join(', ')}',
        );
      }
      return MapEntry(unknownValue, enumValues.values.first);
    },
  ).key;
}

const _$MessageTypeEnumMap = {
  MessageType.FILL_PASSWORD: 'FILL_PASSWORD',
  MessageType.INCORRECT_PASSWORD_ATTEMPT: 'INCORRECT_PASSWORD_ATTEMPT',
  MessageType.INCORRECT_PASSWORD_FAILURE: 'INCORRECT_PASSWORD_FAILURE',
  MessageType.CORRECT_PASSWORD: 'CORRECT_PASSWORD',
  MessageType.LOST_SERVER_CONNECTION: 'LOST_SERVER_CONNECTION',
  MessageType.REGISTER_PACKET: 'REGISTER_PACKET',
  MessageType.TIMED_OUT_REGISTRATION: 'TIMED_OUT_REGISTRATION',
};

HashedPasswordPacket _$HashedPasswordPacketFromJson(
        Map<String, dynamic> json) =>
    HashedPasswordPacket()
      ..hashedPassword = HashedPassword.fromJson(
          json['hashedPassword'] as Map<String, dynamic>);

Map<String, dynamic> _$HashedPasswordPacketToJson(
        HashedPasswordPacket instance) =>
    <String, dynamic>{
      'hashedPassword': instance.hashedPassword,
    };
