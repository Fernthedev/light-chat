import 'dart:convert';
import 'dart:core';
import 'dart:io';
import 'dart:typed_data';

import 'package:light_chat_core/core.dart';
import 'package:light_chat_core/packet_io.dart';
import 'package:light_chat_core/packets_codecs.dart';

import 'package:pointycastle/api.dart';

import 'EventListener.dart';
import 'data/serverdata.dart';

typedef EventCallback<T> = void Function(T data);

class EventType<T> {
  static const REGISTER_EVENT = EventType<ServerData>();
  static const CONNECT_EVENT = EventType<ServerData>();
  static const DISCONNECT_EVENT = EventType<ServerData>();
  static const ERROR_EVENT = EventType<Error>();

  const EventType();
}

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

  bool disconnecting = false;

  ServerData get serverData => _serverData;

  final List<PacketListener> packetListeners = <PacketListener>[];

  final Map<EventType<dynamic>, List<EventCallback<dynamic>>> eventListeners =
      {};

  /// True when the server sucessfully establishes the connection and the server registered the info.
  bool _registered = false;
  bool _connected = false;

  bool get connected => _connected;
  set connected(bool val) {
    _connected = val;
    runCallbacks(EventType.CONNECT_EVENT, serverData);
  }

  bool get registered => _registered;
  set registered(bool val) {
    _registered = val;
    runCallbacks(EventType.REGISTER_EVENT, serverData);
  }

  PacketEventHandler eventHandler;

  ConnectedPacket connectedPacketInfo;

  Client(this.connectedPacketInfo) {
    PacketRegistry.registerDefaultPackets();
    eventHandler = PacketEventHandler(this);
  }

  Future<void> initializeConnection(ServerData serverData) async {
    try {
      _registered = false;
      _connected = false;

      _serverData = serverData;

      var ip = serverData.ip;
      var port = serverData.port;

      _encoder = EncryptedJSONObjectEncoder(this, lineEndStringEncoder);
      _decoder = EncryptedJSONObjectDecoder(this);

      print('Attempt connection $ip:$port');

      socket = await Socket.connect(ip, port);

      print('Connected');
      connected = true;
      print('Making key');
      _key = EncryptionUtil.generateSecretKeyParam();
      _registered = false;
      print('Key generated');

      socket.setOption(SocketOption.tcpNoDelay, true);

      socket.listen(onReceive, onError: (e) async {
        print('Server error: $e');
        await runCallbacks(EventType.ERROR_EVENT, e);
      }, onDone: onDoneEvent);
    } catch (e) {
      print('Server error: $e');
      await runCallbacks(EventType.ERROR_EVENT, e);
    }
  }

  void registerCallback<T>(EventType<T> eventType, EventCallback<T> callback) {
    eventListeners.putIfAbsent(eventType, () => <EventCallback<dynamic>>[]);

    var list = eventListeners[eventType];

    list.add(callback);
  }

  Future<void> runCallbacks<T>(EventType<T> eventType, T data) async {
    var list = eventListeners[eventType];

    if (list != null) {
      for (var callback in list) {
        callback(data);
      }
    }

    return Future.value();
  }

  Future<void> onReceive(dynamic data) async {
    if (data is Uint8List) {
      var list = data;

      var decodedObjects = <Object>[];
      await _decoder.decode(list, decodedObjects);

      for (Packet packet in decodedObjects) {
        eventHandler.received(packet);
      }

      //Object msgObject = bd.getUint8(0);

//    socket.writeln(new String.fromCharCodes(stream).trim() + '\n');

    }
  }

  Future<void> sendMessage(String line) async {
    Packet messagePacket = MessagePacket.create(line);

    if (line.startsWith('/')) {
      line = line.substring(line.indexOf('/') + 1);
      messagePacket = CommandPacket.create(line);
    }

    if (line == '' || line.isEmpty) return;

    await send(messagePacket);

    return Future.value();
  }

  void onDoneEvent() {}

  // Future<void> flush() async {
  //   await socket.done.then((t) {
  //     socket.flush();
  //   });
  //   return Future.value();
  // }

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
      socket.add(data);
    }

    // await socket.flush();

    return Future.value();
  }

  Future<void> send(AcceptablePacketTypes packet, [bool encrypt = true]) async {
    // if (socketFuture != null) {
    //   socketFuture.whenComplete(() {
    //     write(packet, encrypt);
    //     flush();
    //   });
    // } else {

    if (packet == null) throw ArgumentError.notNull('packet');

    Variables.printDebug('Sending $packet');

    await write(packet, encrypt);
    // await flush();
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
    packetListeners.add(listener);
  }

  void removePacketListener(PacketListener listener) {
    packetListeners.remove(listener);
  }

  void handlePacket(Packet p, [Object result]) async {
    for (var packetListener in packetListeners) {
      packetListener.handle(p, result);
    }
  }

  Future<void> close() async {
    disconnecting = true;

    if (socket != null) {
      print('Closing socket');
      await socket.close();
    }
    print('Closed');

    disconnecting = false;
    await runCallbacks(EventType.DISCONNECT_EVENT, serverData);

    return Future.value();
  }
}
