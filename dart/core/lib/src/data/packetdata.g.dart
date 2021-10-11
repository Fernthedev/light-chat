// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'packetdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VersionDataString _$VersionDataStringFromJson(Map<String, dynamic> json) =>
    VersionDataString(
      json['version'] as String,
      json['minVersion'] as String,
    );

Map<String, dynamic> _$VersionDataStringToJson(VersionDataString instance) =>
    <String, dynamic>{
      'version': instance.version,
      'minVersion': instance.minVersion,
    };

HashedPassword _$HashedPasswordFromJson(Map<String, dynamic> json) =>
    HashedPassword(
      json['password'] as String,
    );

Map<String, dynamic> _$HashedPasswordToJson(HashedPassword instance) =>
    <String, dynamic>{
      'password': instance.password,
    };
