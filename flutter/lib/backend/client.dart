import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:chatclientflutter/backend/packethandler.dart';
import 'package:chatclientflutter/util/serverdata.dart';
import 'package:protobuf/protobuf.dart';

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

  pprint(data) {
    if (data is Map) {
      data = json.encode(data);
    }
    print(data);
    return data;
  }

  List<int> stream = List<int>();


  onReceive(dynamic data) {
    List<int> curData;
    if(data is Uint8List) {
      Uint8List list = data;
      stream.addAll(list);
      curData = list;
    }else{
      curData = Uint8List.view(data);
      stream.addAll(curData);
    }


    print("The data received is $curData \nand the full list is $stream");

    finishReceive();
  }


  void finishReceive() {
    print("\nFinished receiving data");
    String msg = Utf8Decoder().convert(stream).trim();
    print("Str msg: $msg");
    GeneratedMessage message = PacketHandler.parseFromBytes(Uint8List.fromList(stream));
    //Object msgObject = bd.getUint8(0);

    String dataStr = stream.toString();

    print("Received $dataStr");
    print("$message was received.");
    socket.writeln(new String.fromCharCodes(stream).trim() + '\n');
    socket.flush();
    stream.clear();
  }

  onDoneEvent() {

  }
}
