import 'package:json_annotation/json_annotation.dart';
import 'package:lombok/lombok.dart';
import 'package:uuid/uuid.dart';

/// This allows the `User` class to access private members in
/// the generated file. The value for this is *.g.dart, where
/// the star denotes the source file name.
part 'serverdata.g.dart';


/// An annotation for the code generator to know that this class needs the
/// JSON serialization logic to be generated.
@JsonSerializable()
@data
class ServerData {
  String ip;
  int port;
  String hashedPassword;

  String _uuid;


  String get uuid {
    if(_uuid == null) {
      _uuid = Uuid().v4();
    }
    return _uuid;
  }

  ServerData(this.ip, this.port,this.hashedPassword) {
    if(_uuid == null) {
      _uuid = Uuid().v4();
    }
  }

  ServerData.fromServer(ServerData serverData) {
    this.ip = serverData.ip;
    this.port = serverData.port;
    this.hashedPassword = serverData.hashedPassword;
    this._uuid = serverData._uuid;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case User.
  factory ServerData.fromJson(Map<String, dynamic> json) => _$ServerDataFromJson(json);

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  Map<String, dynamic> toJson() => _$ServerDataToJson(this);

  bool operator == (other) => other.uuid == uuid;

  @override
  String toString() {
    return "ServerData: $ip:$port/$hashedPassword {$uuid}";
  }

}