import 'dart:convert';
import 'dart:io';
import 'package:light_chat_core/core.dart';
import 'package:light_chat_core/packet_io.dart';
import 'package:light_chat_core/packets_codecs.dart';
import 'package:light_chat_core/packets.dart';

import 'package:lombok/lombok.dart';

part 'multicast.g.dart';

// TODO: INCOMPLETE. FINISH IMPLEMENTATION FOLLOWING THE JAVA SERVER IMPLEMENTATION
class Multicast {
  int _count = 4;

  int get count => _count;

  set count(int value) {
    _count = value;
  }

  Future<MulticastData> startChecking() async {
    await RawDatagramSocket.bind(InternetAddress.anyIPv4, 4446)
        .then((RawDatagramSocket s) {
      s.joinMulticast(InternetAddress(Variables.multicastIP));

      for (var i = 0; i < count; i++) {
        s.listen((RawSocketEvent e) {
          final dg = s.receive();
          if (dg == null) {
            print('Discarding null dg.');
          } else {
            var codec = new Utf8Codec();
            final mess = codec.decode(dg.data);
            print('Received $mess!');
          }
        });
      }

      s.close();
    });
  }

  void run() async {
    await startChecking();
  }
}

@data
class MulticastData with _$MulticastDataLombok {
  MulticastData(this.address, this.version, this.port, this.clientNumbers);

  final String address;

  final String version;
  final int port;

  int clientNumbers = 0;
  List<String> clients;
}
