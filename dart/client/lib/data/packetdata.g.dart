// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'packetdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VersionDataString _$VersionDataStringFromJson(Map<String, dynamic> json) {
  return VersionDataString(
    json['version'] as String,
    json['minVersion'] as String,
  );
}

Map<String, dynamic> _$VersionDataStringToJson(VersionDataString instance) =>
    <String, dynamic>{
      'version': instance.version,
      'minVersion': instance.minVersion,
    };

HashedPassword _$HashedPasswordFromJson(Map<String, dynamic> json) {
  return HashedPassword(
    json['password'] as String,
  );
}

Map<String, dynamic> _$HashedPasswordToJson(HashedPassword instance) =>
    <String, dynamic>{
      'password': instance.password,
    };

// **************************************************************************
// DataGenerator
// **************************************************************************

abstract class _$VersionDataLombok {
  /// Field
  Version version;
  Version minVersion;
  int AES_KEY_SIZE;

  /// Setter

  void setVersion(Version version) {
    this.version = version;
  }

  void setMinVersion(Version minVersion) {
    this.minVersion = minVersion;
  }

  /// Getter
  Version getVersion() {
    return version;
  }

  Version getMinVersion() {
    return minVersion;
  }

  int getAES_KEY_SIZE() {
    return AES_KEY_SIZE;
  }
}
