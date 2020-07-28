// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'serverdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ServerData _$ServerDataFromJson(Map<String, dynamic> json) {
  return ServerData(
    json['ip'] as String,
    json['port'] as int,
    json['hashedPassword'] as String,
  )..uuid = json['uuid'] as String;
}

Map<String, dynamic> _$ServerDataToJson(ServerData instance) =>
    <String, dynamic>{
      'ip': instance.ip,
      'port': instance.port,
      'hashedPassword': instance.hashedPassword,
      'uuid': instance.uuid,
    };

// **************************************************************************
// DataGenerator
// **************************************************************************

abstract class _$ServerDataLombok {
  /// Field
  String ip;
  int port;
  String _hashedPassword;
  String _uuid;
  dynamic hashedPassword;
  String hashedPasswordDoHash;
  String uuid;

  /// Setter

  void setIp(String ip) {
    this.ip = ip;
  }

  void setPort(int port) {
    this.port = port;
  }

  void set_hashedPassword(String _hashedPassword) {
    this._hashedPassword = _hashedPassword;
  }

  void set_uuid(String _uuid) {
    this._uuid = _uuid;
  }

  void setHashedPassword(dynamic hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  void setHashedPasswordDoHash(String hashedPasswordDoHash) {
    this.hashedPasswordDoHash = hashedPasswordDoHash;
  }

  void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /// Getter
  String getIp() {
    return ip;
  }

  int getPort() {
    return port;
  }

  String get_hashedPassword() {
    return _hashedPassword;
  }

  String get_uuid() {
    return _uuid;
  }

  dynamic getHashedPassword() {
    return hashedPassword;
  }

  String getHashedPasswordDoHash() {
    return hashedPasswordDoHash;
  }

  String getUuid() {
    return uuid;
  }
}
