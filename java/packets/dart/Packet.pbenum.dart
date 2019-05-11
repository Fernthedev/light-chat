///
//  Generated code. Do not modify.
//  source: Packet.proto
///
// ignore_for_file: camel_case_types,non_constant_identifier_names,library_prefixes,unused_import,unused_shown_name

// ignore_for_file: UNDEFINED_SHOWN_NAME,UNUSED_SHOWN_NAME
import 'dart:core' as $core show int, dynamic, String, List, Map;
import 'package:protobuf/protobuf.dart' as $pb;

class SelfMessageType extends $pb.ProtobufEnum {
  static const SelfMessageType FillPasswordPacket = SelfMessageType._(0, 'FillPasswordPacket');
  static const SelfMessageType LostServerConnectionPacket = SelfMessageType._(1, 'LostServerConnectionPacket');
  static const SelfMessageType RegisterPacket = SelfMessageType._(2, 'RegisterPacket');
  static const SelfMessageType TimedOutRegistrationPacket = SelfMessageType._(3, 'TimedOutRegistrationPacket');
  static const SelfMessageType PongPacket = SelfMessageType._(4, 'PongPacket');
  static const SelfMessageType PingReceive = SelfMessageType._(5, 'PingReceive');
  static const SelfMessageType PingPacket = SelfMessageType._(6, 'PingPacket');
  static const SelfMessageType AuthenticateMessage = SelfMessageType._(7, 'AuthenticateMessage');
  static const SelfMessageType DisconnectPacket = SelfMessageType._(8, 'DisconnectPacket');

  static const $core.List<SelfMessageType> values = <SelfMessageType> [
    FillPasswordPacket,
    LostServerConnectionPacket,
    RegisterPacket,
    TimedOutRegistrationPacket,
    PongPacket,
    PingReceive,
    PingPacket,
    AuthenticateMessage,
    DisconnectPacket,
  ];

  static final $core.Map<$core.int, SelfMessageType> _byValue = $pb.ProtobufEnum.initByValue(values);
  static SelfMessageType valueOf($core.int value) => _byValue[value];

  const SelfMessageType._($core.int v, $core.String n) : super(v, n);
}

