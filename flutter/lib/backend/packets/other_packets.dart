import 'package:flutter/cupertino.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:lightchat_client/backend/packets/packets.dart';
import 'package:lightchat_client/backend/transport/packetwrapper.dart';
import 'package:lombok/lombok.dart';

part 'other_packets.g.dart';

@JsonSerializable()
@ToString()
class CommandPacket extends Packet {

  static final CommandPacket constant = CommandPacket();

  @protected
  CommandPacket() : super.setName("COMMAND_PACKET");

  String message;

  factory CommandPacket.create(String message) {
    CommandPacket packet = CommandPacket();

    packet.message = message;

    return packet;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory CommandPacket.fromJson(Map<String, dynamic> json) => _$CommandPacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return CommandPacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$CommandPacketToJson(this);
}


@JsonSerializable()
@ToString()
class IllegalConnection extends Packet {

  static final IllegalConnection constant = IllegalConnection();

  @protected
  IllegalConnection() : super.setName("ILLEGAL_CONNECTION");

  String message;

  factory IllegalConnection.create(String message) {
    IllegalConnection packet = IllegalConnection();

    packet.message = message;

    return packet;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory IllegalConnection.fromJson(Map<String, dynamic> json) => _$IllegalConnectionFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return IllegalConnection.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$IllegalConnectionToJson(this);
}

@JsonSerializable()
@ToString()
class MessagePacket extends Packet {

  static final MessagePacket constant = MessagePacket();

  @protected
  MessagePacket() : super.setName("MESSAGE_PACKET");

  String message;

  factory MessagePacket.create(String message) {
    MessagePacket packet = MessagePacket();

    packet.message = message;

    return packet;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory MessagePacket.fromJson(Map<String, dynamic> json) => _$MessagePacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return MessagePacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$MessagePacketToJson(this);
}

@JsonSerializable()
@ToString()
class SelfMessagePacket extends Packet {

  static final SelfMessagePacket constant = SelfMessagePacket();

  @protected
  SelfMessagePacket() : super.setName("SELF_MESSAGE_PACKET");

  MessageType messageType;

  factory SelfMessagePacket.create(MessageType messageType) {
    SelfMessagePacket packet = SelfMessagePacket();

    packet.messageType = messageType;

    return packet;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory SelfMessagePacket.fromJson(Map<String, dynamic> json) => _$SelfMessagePacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return SelfMessagePacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$SelfMessagePacketToJson(this);
}

enum MessageType {
  FILL_PASSWORD,
  LOST_SERVER_CONNECTION,
  REGISTER_PACKET,
  TIMED_OUT_REGISTRATION
}