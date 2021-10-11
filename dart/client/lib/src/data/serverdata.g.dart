// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'serverdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ServerData _$ServerDataFromJson(Map<String, dynamic> json) => ServerData(
      json['ip'] as String,
      json['port'] as int,
      json['hashedPassword'] as String?,
    )..uuid = json['uuid'] as String;

Map<String, dynamic> _$ServerDataToJson(ServerData instance) =>
    <String, dynamic>{
      'ip': instance.ip,
      'port': instance.port,
      'hashedPassword': instance.hashedPassword,
      'uuid': instance.uuid,
    };
