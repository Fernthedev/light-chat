import 'dart:convert';

import 'package:crypto/crypto.dart';
import 'package:light_chat_client/transport/packetwrapper.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:light_chat_client/util/encryption/encryption.dart';
import 'package:lombok/lombok.dart';
import 'package:version/version.dart';

part 'packetdata.g.dart';

@data
class VersionData {
  Version version;
  Version minVersion;

  VersionData(this.version, this.minVersion);

  VersionData.fromString(String version, String minVersion) {
    this.version = Version.parse(version);
    this.minVersion = Version.parse(minVersion);
  }

  VersionData.fromVersionDataString(VersionDataString versionDataString)
      : this.fromString(
            versionDataString.version, versionDataString.minVersion);

  VersionDataString toDataString() {
    return VersionDataString.fromVersionData(this);
  }

  VersionRange getVersionRangeStatusSingle(VersionData otherVersion) {
    return getVersionRangeStatus(this, otherVersion);
  }

  static VersionRange getVersionRangeStatus(
      VersionData versionData, VersionData otherVersion) {
    var current = versionData.version;
    var minCurrent = versionData.minVersion;

    var otherCurrent = otherVersion.version;
    var otherMin = otherVersion.minVersion;

    /// Current version is smaller than the server's required minimum
    if (current < otherMin) {
      return VersionRange.WE_ARE_LOWER;
    } else

    /// Current version is larger than server's minimum version
    if (minCurrent > otherCurrent) {
      return VersionRange.WE_ARE_HIGHER;
    } else {
      return VersionRange.MATCH_REQUIREMENTS;
    }
  }

  static final int AES_KEY_SIZE = 256;
}

@JsonSerializable(nullable: false)
class VersionDataString implements JsonSerializableClass {
  final String version;
  final String minVersion;

  VersionDataString.fromVersionData(VersionData versionData)
      : this(versionData.version.toString(), versionData.minVersion.toString());

  VersionDataString(this.version, this.minVersion);

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory VersionDataString.fromJson(Map<String, dynamic> json) =>
      _$VersionDataStringFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return VersionDataString.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$VersionDataStringToJson(this);
}

enum VersionRange {
  WE_ARE_HIGHER,
  MATCH_REQUIREMENTS,
  WE_ARE_LOWER,
}

@JsonSerializable(explicitToJson: true, nullable: false)
class HashedPassword extends JsonSerializableClass {
  String password;

  HashedPassword.fromHash(this.password);

  HashedPassword(String password) {
    password = EncryptionUtil.toSha256(password);
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory HashedPassword.fromJson(Map<String, dynamic> json) =>
      _$HashedPasswordFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return HashedPassword.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$HashedPasswordToJson(this);
}
