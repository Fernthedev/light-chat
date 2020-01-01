import 'package:lightchat_client/backend/transport/packetwrapper.dart';

abstract class AcceptablePacketTypes implements JsonSerializableClass {
  String getPacketName();
}