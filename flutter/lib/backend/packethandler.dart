import 'package:chatclientflutter/packets/Packet.pb.dart';
import 'package:protobuf/protobuf.dart';
import 'dart:io';

class PacketHandler {

  static List<GeneratedMessage> packets = List<GeneratedMessage>();

  PacketHandler() {
    packets.clear();
    print("Listing packets");
    packets.add(AutoCompletePacket.getDefault());
    packets.add(ConnectedPacket.getDefault());
    packets.add(IllegalConnectionPacket.getDefault());
    packets.add(SelfMessagePacket.getDefault());
    packets.add(MessagePacket.getDefault());
    packets.add(RequestInfoPacket.getDefault());


  }


  static GeneratedMessage parseFromBytes(List<int> bytes) {
    GeneratedMessage generatedMessage;

    for(int i = 0; i < packets.length; i++) {
      GeneratedMessage checkMessage = packets[i];

      try {
        generatedMessage = checkMessage.createEmptyInstance();

        generatedMessage.mergeFromBuffer(bytes);

        _validateMessage(generatedMessage);

        print("Checking if $generatedMessage is message");

        if (!generatedMessage.isInitialized())
          continue;
        else
          break;
      } catch(e) {
        String check = checkMessage.runtimeType.toString();
        print("Error while attempting to parse $check");
        int length = packets.length;
        print("Int $i length: $length");
        print("Error: $e");

        if(i == packets.length -1) {
          print("Could not parse packet");
          generatedMessage = null;
          rethrow;
        }
        continue;
      }
    }

    String runtimeType = generatedMessage.runtimeType.toString();
    print("It is return $runtimeType");
    return generatedMessage;
  }

  static _validateMessage(GeneratedMessage generatedMessage) {
    generatedMessage.check();
  }
}
