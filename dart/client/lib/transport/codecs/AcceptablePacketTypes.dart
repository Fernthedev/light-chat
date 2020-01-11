import 'package:light_chat_client/transport/packetwrapper.dart';

abstract class AcceptablePacketTypes implements JsonSerializableClass {
  String getPacketName();
}