import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:chatclientflutter/backend/packethandler.dart';
import 'package:chatclientflutter/util/serverdata.dart';

class Client {
  Socket socket;

  void initializeConnection(ServerData serverData) async {
    PacketHandler();

    String ip = serverData.ip;
    int port = serverData.port;

    print("Attempt connection $ip:$port");

    Socket.connect(ip, port).then((Socket sock) {
      print("Connected");
      socket = sock;
      

      socket.setOption(SocketOption.tcpNoDelay, true);

      socket.listen(onReceive,
          onError: (e) {
            print('Server error: $e');
          },
          onDone: onDoneEvent,
          cancelOnError: false);

    });

    print(json);
    if(socket != null) {
      //socket.writeln(base64.encode(utf8.encode(json)));
      //Connect standard in to the socket
    }
    //stdin.listen((data) => socket.write(new String.fromCharCodes(data).trim() + '\n'));
  }



  onReceive(dynamic data) {
    if(data is Uint8List) {
      Uint8List list = data;


      print("The data received is ${data is Uint8List}");

      String msg = Utf8Decoder().convert(list).trim();
      print("Str msg: $msg");
      print("Class: ${data.runtimeType}");
      //Object msgObject = bd.getUint8(0);

//    socket.writeln(new String.fromCharCodes(stream).trim() + '\n');
      socket.flush();
    }
  }

  onDoneEvent() {

  }
}
