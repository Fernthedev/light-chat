import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:lightchat_client/assets/variables.dart';
import 'package:lightchat_client/backend/EventListener.dart';
import 'package:lightchat_client/backend/packethandler.dart';
import 'package:lightchat_client/backend/packets/handshake_packets.dart';
import 'package:lightchat_client/backend/packets/latency_packets.dart';
import 'package:lightchat_client/backend/packets/packets.dart';
import 'package:lightchat_client/backend/transport/codecs/AcceptablePacketTypes.dart';
import 'package:lightchat_client/backend/transport/codecs/codecs.dart';
import 'package:lightchat_client/backend/transport/packet_registry.dart';
import 'package:lightchat_client/backend/transport/packetwrapper.dart';
import 'package:lightchat_client/data/serverdata.dart';
import 'package:lightchat_client/util/encryption.dart';
import 'package:pointycastle/api.dart';

class Client implements IKeyEncriptionHolder {
  Socket socket;
  KeyParameter _key;
  EncryptedJSONObjectEncoder _encoder;
  EncryptedJSONObjectDecoder _decoder;

  LineEndStringEncoder lineEndStringEncoder = new LineEndStringEncoder(utf8);

  DateTime startTime;
  DateTime endTime;

  int milliPingDelay;

  final List<PacketListener> futureList = List();

  bool registered;
  PacketEventHandler eventHandler;

  ConnectedPacket connectedPacketInfo;


  Client(this.connectedPacketInfo) {
    PacketRegistry.registerDefaultPackets();
    eventHandler = PacketEventHandler(this);
  }

  void initializeConnection(ServerData serverData) async {
    PacketHandler();

    String ip = serverData.ip;
    int port = serverData.port;

    _encoder = new EncryptedJSONObjectEncoder(this, lineEndStringEncoder);
    _decoder = new EncryptedJSONObjectDecoder(this);

    print("Attempt connection $ip:$port");

    Socket.connect(ip, port).then((Socket sock) {
      print("Connected");
      socket = sock;
      _key = EncryptionUtil.generateSecretKeyParam();
      registered = false;

      socket.setOption(SocketOption.tcpNoDelay, true);

      socket.listen(onReceive,
          onError: (e) {
            print('Server error: $e');
          },
          onDone: onDoneEvent,
          cancelOnError: false);

    });

    print("Sending: ${UnencryptedPacketWrapper(InitialHandshakePacket.create(null, Variables.versionData)).toJson()}");
    //stdin.listen((data) => socket.write(new String.fromCharCodes(data).trim() + '\n'));
  }



  onReceive(dynamic data) {
    if(data is Uint8List) {
      Uint8List list = data;

      List<Object> decodedObjects = List();
      _decoder.decode(list, decodedObjects);
      eventHandler.received(decodedObjects[0]);
      
      //Object msgObject = bd.getUint8(0);

//    socket.writeln(new String.fromCharCodes(stream).trim() + '\n');
      flush();
    }
  }

  onDoneEvent() {

  }

  void flush() {
    socket.done.then((t) {
      socket.flush();
    });
  }

  Future socketFuture;

  void write(AcceptablePacketTypes packet, [bool encrypt = true]) {
    AcceptablePacketTypes json;

    if(encrypt) {
      json = packet;
    } else {
      json = UnencryptedPacketWrapper(packet);
    }

    List<Object> encodedMessages = List();
    _encoder.encode(json, encodedMessages);

    List<Uint8List> dataList = List();

    for (Object o in encodedMessages) dataList.add(o);

    for (Uint8List data in dataList) {
      print("The sent data is ${utf8.decode(data)}");
      socket.add(data);
    }
  }

  void send(AcceptablePacketTypes packet, [bool encrypt = true]) async {

    if(socketFuture != null) socketFuture.whenComplete(() {
      write(packet, encrypt);
      flush();
    });
    else {
      write(packet, encrypt);
      flush();
    }


  }

  @override
  KeyParameter getKey() {
    return _key;
  }

  @override
  bool isEncryptionKeyRegistered() {
    return _key != null;
  }

  void handlePacket(Packet p, [Object result]) async {
    for (PacketListener packetListener in futureList) {
      packetListener.handle(p, result);
    }
  }

  void close() async {
    print("Closing socket");
    socket.close();
    print("Closed");
  }
}
