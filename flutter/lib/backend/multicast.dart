import 'dart:convert';
import 'dart:io';

import 'package:lombok/lombok.dart';

import '../main.dart';

part 'multicast.g.dart';

class Multicast extends Runnable {
  int _count = 4;

  int get count => _count;

  set count(int value) {
    _count = value;
  }

  Future<MulticastData> startChecking() async {
    RawDatagramSocket.bind(InternetAddress.anyIPv4, 4446)
        .then((RawDatagramSocket s) {
      s.joinMulticast(new InternetAddress("230.0.0.0"));

      for (int i = 0; i < count; i++) {
        s.listen((RawSocketEvent e) {
          final dg = s.receive();
          if (dg == null) {
            print("Discarding null dg.");
          } else {
            var codec = new Utf8Codec();
            final mess = codec.decode(dg.data);
            print("Received $mess!");
          }
        });

      }

      s.close();
    });
  }

  @override
  void run() async {
    await startChecking();
  }
}

@data
class MulticastData with _$MulticastDataLombok {
  MulticastData(this._address, this._port, this._clientNumbers);

  String _address;

  String _version;
  int _port;

  int _clientNumbers = 0;
  List<String> _clients;
}
