
import 'package:json_annotation/json_annotation.dart';

import 'packets.dart';

part 'latency_packets.g.dart';

@JsonSerializable()
class PingPacket extends Packet {

  static final PingPacket constant = PingPacket();


  PingPacket() : super.setName('PING_PACKET');

  factory PingPacket.create() {
    var pingPacket = PingPacket();

    return pingPacket;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory PingPacket.fromJson(Map<String, dynamic> json) => _$PingPacketFromJson(json);

  @override
  PingPacket fromJson(Map<String, dynamic> json) {
    return PingPacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$PingPacketToJson(this);
}

@JsonSerializable()
class PingReceive extends Packet {

  static final PingReceive constant = PingReceive();


  PingReceive() : super.setName('PING_RECEIVE');

  factory PingReceive.create() {
    var pingPacket = PingReceive();

    return pingPacket ;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory PingReceive.fromJson(Map<String, dynamic> json) => _$PingReceiveFromJson(json);

  @override
  PingReceive fromJson(Map<String, dynamic> json) {
    return PingReceive.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$PingReceiveToJson(this);
}

@JsonSerializable()
class PongPacket extends Packet {

  static final PongPacket constant = PongPacket();

  PongPacket() : super.setName('PONG_PACKET');

  factory PongPacket.create() {
    var pingPacket = PongPacket();

    return pingPacket;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory PongPacket.fromJson(Map<String, dynamic> json) => _$PongPacketFromJson(json);

  @override
  PongPacket fromJson(Map<String, dynamic> json) {
    return PongPacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$PongPacketToJson(this);
}
