import 'dart:convert';
import 'dart:io';
import 'package:light_chat_core/core.dart';
import 'package:light_chat_core/packet_io.dart';
import 'package:light_chat_core/packets_codecs.dart';
import 'package:light_chat_core/packets.dart';

// TODO: INCOMPLETE. FINISH IMPLEMENTATION FOLLOWING THE JAVA SERVER IMPLEMENTATION
class Multicast {
  int count = 4;

  Future<MulticastData?> startChecking() async {
    return await RawDatagramSocket.bind(InternetAddress.anyIPv4, 4446)
        .then((RawDatagramSocket s) {
      s.joinMulticast(InternetAddress(Variables.multicastIP));

      for (var i = 0; i < count; i++) {
        s.listen((RawSocketEvent e) {
          final dg = s.receive();
          if (dg == null) {
            print('Discarding null dg.');
          } else {
            var codec = Utf8Codec();
            final mess = codec.decode(dg.data);
            print('Received $mess!');
          }
        });
      }

      s.close();
      return Future.value();
    });
  }

  void run() async {
    await startChecking();
  }
}

class MulticastData {
  MulticastData(this.address, this.version, this.port, this.clientNumbers);

  final String address;
  final String version;
  final int port;
  int clientNumbers = 0;
  late List<String> clients;
}
