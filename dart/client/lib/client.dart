import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:light_chat_client/packets/handshake_packets.dart';
import 'package:light_chat_client/packets/other_packets.dart';
import 'package:light_chat_client/packets/packets.dart';
import 'package:light_chat_client/transport/codecs/AcceptablePacketTypes.dart';
import 'package:light_chat_client/transport/codecs/codecs.dart';
import 'package:light_chat_client/transport/packet_registry.dart';
import 'package:light_chat_client/transport/packetwrapper.dart';
import 'package:light_chat_client/util/encryption/encryption.dart';
import 'package:light_chat_client/variables.dart';
import 'package:pedantic/pedantic.dart';
import 'package:pointycastle/api.dart';

import 'EventListener.dart';
import 'data/serverdata.dart';

class Client implements IKeyEncriptionHolder {
  Socket socket;
  KeyParameter _key;
  EncryptedJSONObjectEncoder _encoder;
  EncryptedJSONObjectDecoder _decoder;

  LineEndStringEncoder lineEndStringEncoder = LineEndStringEncoder(utf8);

  DateTime startTime;
  DateTime endTime;

  int milliPingDelay;
  ServerData _serverData;

  ServerData get serverData => _serverData;

  final List<PacketListener> futureList = <PacketListener>[];
  final List<Function> _registerCallBackList = <Function(ServerData)>[];
  final List<Function(ServerData)> _connectCallBackList =
      <Function(ServerData)>[];

  /// True when the server sucessfully establishes the connection and the server registered the info.
  bool _registered = false;
  bool _connected = false;

  bool get connected => _connected;
  set connected(bool val) {
    _connected = val;
    _runConnectCallbacks();
  }

  bool get registered => _registered;
  set registered(bool val) {
    _registered = val;
    _runRegisterCallbacks();
  }

  PacketEventHandler eventHandler;

  ConnectedPacket connectedPacketInfo;

  Client(this.connectedPacketInfo) {
    PacketRegistry.registerDefaultPackets();
    eventHandler = PacketEventHandler(this);
  }

  Future<void> initializeConnection(ServerData serverData) async {
    _registered = false;
    _connected = false;

    _serverData = serverData;

    var ip = serverData.ip;
    var port = serverData.port;

    _encoder = EncryptedJSONObjectEncoder(this, lineEndStringEncoder);
    _decoder = EncryptedJSONObjectDecoder(this);

    print('Attempt connection $ip:$port');

    await Socket.connect(ip, port).then((Socket sock) {
      print('Connected');
      connected = true;
      socket = sock;
      _key = EncryptionUtil.generateSecretKeyParam();
      _registered = false;

      socket.setOption(SocketOption.tcpNoDelay, true);

      socket.listen(onReceive, onError: (e) {
        print('Server error: $e');
      }, onDone: onDoneEvent, cancelOnError: false);
    });

    return Future.value();
  }

  void onRegister(Function(ServerData) callback) {
    _registerCallBackList.add(callback);
  }

  void removeOnRegister(Function(ServerData) callback) {
    _registerCallBackList.remove(callback);
  }

  void onConnect(Function(ServerData) callback) {
    _connectCallBackList.add(callback);
  }

  void removeOnConnect(Function(ServerData) callback) {
    _connectCallBackList.remove(callback);
  }

  void _runConnectCallbacks() async {
    for (var callback in _connectCallBackList) {
      if (Variables.debug) print('Running the callback');
      unawaited(runCallback(callback, arguments: serverData));
    }
  }

  void _runRegisterCallbacks() async {
    for (Function() callback in _registerCallBackList) {
      if (Variables.debug) print('Running the callback');
      unawaited(runCallback(callback, arguments: serverData));
    }
  }

  Future<void> runCallback(Function function, {Object arguments}) async {
    function(arguments);
    return Future.value();
  }

  Future<void> onReceive(dynamic data) async {
    await flush();
    if (data is Uint8List) {
      var list = data;

      var decodedObjects = <Object>[];
      await _decoder.decode(list, decodedObjects);
      eventHandler.received(decodedObjects[0]);

      //Object msgObject = bd.getUint8(0);

//    socket.writeln(new String.fromCharCodes(stream).trim() + '\n');

    }
  }

  void sendMessage(String line) {
    Packet messagePacket = MessagePacket.create(line);

    if (line.startsWith('/')) {
      line = line.substring(line.indexOf('/'));
      messagePacket = CommandPacket.create(line);
    }

    if (line == '' || line.isEmpty) return;

    send(messagePacket);
  }

  void onDoneEvent() {}

  Future<void> flush() async {
    await socket.done.then((t) {
      socket.flush();
    });
    return Future.value();
  }

  Future<void> write(AcceptablePacketTypes packet,
      [bool encrypt = true]) async {
    AcceptablePacketTypes json;

    if (encrypt) {
      json = packet;
    } else {
      json = UnencryptedPacketWrapper(packet);
    }

    var encodedMessages = <Object>[];
    await _encoder.encode(json, encodedMessages);

    var dataList = <Uint8List>[];

    for (var o in encodedMessages) {
      dataList.add(o);
    }

    for (var data in dataList) {
      if (Variables.debug) {
        ('The sent data is ${utf8.decode(data)}');
      }
      socket.add(data);
    }
    return Future.value();
  }

  Future<void> send(AcceptablePacketTypes packet, [bool encrypt = true]) async {
    // if (socketFuture != null) {
    //   socketFuture.whenComplete(() {
    //     write(packet, encrypt);
    //     flush();
    //   });
    // } else {
    await write(packet, encrypt);
    await flush();
    // }

    return Future.value();
  }

  @override
  KeyParameter getKey() {
    return _key;
  }

  @override
  bool isEncryptionKeyRegistered() {
    return _key != null;
  }

  void addPacketListener(PacketListener listener) {
    futureList.add(listener);
  }

  void removePacketListener(PacketListener listener) {
    futureList.remove(listener);
  }

  void handlePacket(Packet p, [Object result]) async {
    for (var packetListener in futureList) {
      packetListener.handle(p, result);
    }
  }

  void close() async {
    print('Closing socket');
    await socket.close();
    print('Closed');
  }
}
