
import 'package:light_chat_client/transport/codecs/AcceptablePacketTypes.dart';
import 'package:light_chat_client/transport/packet_registry.dart';
import 'package:light_chat_client/transport/packetwrapper.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:lombok/lombok.dart';


part 'packets.g.dart';

abstract class Packet implements AcceptablePacketTypes, JsonSerializableClass  {

  Packet();

  @JsonKey(ignore: true)
  String packetName;

  Packet.setName(this.packetName);


  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory Packet.fromJson(Map<String, dynamic> json) {
    return PacketRegistry.getPacketInstanceFromRegistry(json[PacketWrapper.getPacketIdentifier()], json[PacketWrapper.getJsonIdentifier()]);
  }

//  /// `toJson` is the convention for a class to declare support for serialization
//  /// to JSON. The implementation simply calls the private, generated
//  /// helper method `_$UserToJson`.
//  Map<String, dynamic> toJson() => _$PacketToJson(this);
  @override
  String getPacketName() {
    return packetName;
  }
}

@JsonSerializable()
@ToString()
class _TemplatePacket extends Packet {

  static final _TemplatePacket constant = _TemplatePacket();

  _TemplatePacket() : super.setName('TEMPLATE_PACKET');

  factory _TemplatePacket.create() {
    var packet = _TemplatePacket();

    return packet;
  }

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory _TemplatePacket.fromJson(Map<String, dynamic> json) => _$_TemplatePacketFromJson(json);

  @override
  JsonSerializableClass fromJson(Map<String, dynamic> json) {
    return _TemplatePacket.fromJson(json);
  }

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  @override
  Map<String, dynamic> toJson() => _$_TemplatePacketToJson(this);
}

