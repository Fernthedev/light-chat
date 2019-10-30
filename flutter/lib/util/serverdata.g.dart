// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'serverdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ServerData _$ServerDataFromJson(Map<String, dynamic> json) {
  return ServerData(json['ip'] as String, json['port'] as int,
      json['hashedPassword'] as String);
}

Map<String, dynamic> _$ServerDataToJson(ServerData instance) =>
    <String, dynamic>{
      'ip': instance.ip,
      'port': instance.port,
      'hashedPassword': instance.hashedPassword
    };

// **************************************************************************
// DataGenerator
// **************************************************************************

abstract class _$ServerDataLombok {
  /// Field
  String ip;
  int port;
  String hashedPassword;
  String _uuid;
  String uuid;

  /// Setter

  void setIp(String ip) {
    this.ip = ip;
  }

  void setPort(int port) {
    this.port = port;
  }

  void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  void set_uuid(String _uuid) {
    this._uuid = _uuid;
  }

  /// Getter
  String getIp() {
    return ip;
  }

  int getPort() {
    return port;
  }

  String getHashedPassword() {
    return hashedPassword;
  }

  String get_uuid() {
    return _uuid;
  }

  String getUuid() {
    return uuid;
  }
}
