import 'package:json_annotation/json_annotation.dart';
import 'package:lightchat_client/backend/transport/packetwrapper.dart';
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

  VersionData.fromVersionDataString(VersionDataString versionDataString) : this.fromString(versionDataString.version, versionDataString.minVersion);

  VersionDataString toDataString() {
    return VersionDataString.fromVersionData(this);
  }
}

@JsonSerializable(nullable: false)
class VersionDataString implements JsonSerializableClass {

  final String version;
  final String minVersion;

  VersionDataString.fromVersionData(VersionData versionData) : this(versionData.version.toString(), versionData.minVersion.toString());

  VersionDataString(this.version, this.minVersion);

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory VersionDataString.fromJson(Map<String, dynamic> json) => _$VersionDataStringFromJson(json);

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