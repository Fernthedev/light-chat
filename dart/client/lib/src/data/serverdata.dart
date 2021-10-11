//import 'package:json_annotation/json_annotation.dart';
//import 'package:lombok/lombok.dart';
//import 'package:uuid/uuid.dart';


import 'package:json_annotation/json_annotation.dart';
import 'package:light_chat_core/core.dart';


import 'package:uuid/uuid.dart';

part 'serverdata.g.dart';

/// An annotation for the code generator to know that this class needs the
/// JSON serialization logic to be generated.
@JsonSerializable(createFactory: true)
class ServerData {
  late String ip;
  late int port;
  String? _hashedPassword;
  String? _uuid;

  String? get hashedPassword => _hashedPassword;
  set hashedPasswordDoHash(String hashedPassword) =>
      _hashedPassword = EncryptionUtil.toSha256(hashedPassword);

  set hashedPassword(String? password) => _hashedPassword = password;

  String get uuid {
    _uuid ??= Uuid().v4();
    return _uuid!;
  }

  set uuid(String uuid) {
    _uuid = uuid;
  }

  ServerData(this.ip, this.port, String? hashedPassword) {
    if (hashedPassword != null) {
      _hashedPassword = EncryptionUtil.toSha256(hashedPassword);
    } else {
      _hashedPassword = null;
    }
  }

  ServerData.fromServer(ServerData serverData) {
    ip = serverData.ip;
    port = serverData.port;
    _hashedPassword = serverData.hashedPassword;
    _uuid = serverData._uuid;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case User.
  factory ServerData.fromJson(Map<String, dynamic> json) {
    ServerData serverData = _$ServerDataFromJson(json);

    serverData._hashedPassword = json['hashedPassword'];

    return serverData;
  }
  // _$ServerDataFromJson(json);

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  Map<String, dynamic> toJson() => _$ServerDataToJson(this);

  @override
  bool operator ==(other) => other is ServerData && other.uuid == uuid;

  String ipPortName() => '$ip:$port';

  @override
  String toString() {
    return 'ServerData: $ip:$port/$hashedPassword {$uuid}';
  }
}
