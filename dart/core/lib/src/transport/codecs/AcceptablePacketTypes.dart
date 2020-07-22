
import '../packetwrapper.dart';

abstract class AcceptablePacketTypes implements JsonSerializableClass {
  String getPacketName();
}
