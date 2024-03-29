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

typedef EventCallback<T> = Future<void> Function(T data);

class EventType<T> {
  static const REGISTER_EVENT = EventType<ServerData>('REGISTER_EVENT');
  static const CONNECT_EVENT = EventType<ServerData>('CONNECT_EVENT');
  static const DISCONNECT_EVENT = EventType<ServerData>('DISCONNECT_EVENT');
  static const ERROR_EVENT = EventType<dynamic>('ERROR_EVENT');

  const EventType(this.eventName);

  final String eventName;
}

class Client implements IKeyEncriptionHolder {
  Socket? socket;
  KeyParameter? _key;
  EncryptedJSONObjectEncoder? _encoder;
  EncryptedJSONObjectDecoder? _decoder;

  LineEndStringEncoder lineEndStringEncoder = LineEndStringEncoder(utf8);

  DateTime? startTime;
  DateTime? endTime;

  int milliPingDelay = -1;
  ServerData? _serverData;

  bool disconnecting = false;

  ServerData? get serverData => _serverData;

  final List<PacketListener> packetListeners = <PacketListener>[];

  final Map<EventType<dynamic>, List<EventCallback<dynamic>>> eventListeners =
      {};

  // Watch event registers
  final Map<dynamic, EventCallback<dynamic>> eventListenerRegistry = {};

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

  late PacketEventHandler eventHandler;

  ConnectedPacket connectedPacketInfo;

  Client(this.connectedPacketInfo) {

    eventHandler = PacketEventHandler(this);
  }

  Future<void> initializeConnection(ServerData serverData) async {
    PacketRegistry.registerDefaultPackets();
    ClassTranslator.init();
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

      socket!.setOption(SocketOption.tcpNoDelay, true);

      socket!.listen(onReceive, onError: (e) async {
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

    var list = eventListeners[eventType]!;

    // Wrap around dynamic event call back to avoid type errors.
    var wrapper = (d) => callback(d as T);

    list.add(wrapper);
    eventListenerRegistry[callback] = wrapper;
  }

  void unregisterCallback<T>(
      EventType<T> eventType, EventCallback<T> callback) {
    if (!eventListeners.containsKey(eventType)) return;

    eventListeners[eventType]!.remove(eventListenerRegistry[callback]);
    eventListenerRegistry.remove(callback);

    if (eventListeners[eventType]!.isEmpty) eventListeners.remove(eventType);
  }

  void unregisterCallbacks<T>(EventCallback<T> callback) {
    eventListeners.forEach((key, value) {
      if (value.contains(callback)) {
        var keyCast = key as EventType<T>;
        unregisterCallback<T>(keyCast, callback);
      }
    });
  }

  void unregisterEvents<T>(EventType<T> eventType) {
    for (var element in eventListeners[eventType]!) {
      unregisterCallback(eventType, element);
    }
    eventListeners.remove(eventType);
  }

  Future<void> runCallbacks<T>(EventType<T> eventType, T data) async {
    var list = eventListeners[eventType];

    if (list != null) {
      return Future.forEach(
          list, (EventCallback<T> callback) async => await callback(data));
    }
  }

  Future<void> onReceive(Uint8List data) async {
    var decodedObjects = <Object>[];
    await _decoder!.decode(data, decodedObjects);

    for (var packet in decodedObjects.whereType<Packet>()) {
      await eventHandler.received(packet);
    }
  }

  Future<void> sendMessage(String line) async {
    Packet messagePacket = MessagePacket.create(line);

    if (line.startsWith('/')) {
      line = line.substring(line.indexOf('/') + 1);
      messagePacket = CommandPacket.create(line);
    }

    if (line == '' || line.isEmpty) return;

    return await send(messagePacket);
  }

  void onDoneEvent() {}

  // Future<void> flush() async {
  //   await socket.done.then((t) {
  //     socket.flush();
  //   });
  //   return Future.value();
  // }

  Future<void> send(AcceptablePacketTypes packet, [bool encrypt = true]) async {
    Variables.printDebug('Sending $packet');

    var json = encrypt ? packet : UnencryptedPacketWrapper(packet);

    var encodedMessages = <Object>[];
    await _encoder!.encode(json, encodedMessages);

    for (var data in encodedMessages.whereType<List<int>>()) {
      socket!.add(data);
    }

    await socket!.flush();

    return Future.value();
  }

  @override
  KeyParameter? getKey() {
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

  void handlePacket(Packet p, [Object? result]) async {
    for (var packetListener in packetListeners) {
      await packetListener(p, result);
    }
  }

  Future<void> close() async {
    disconnecting = true;

    if (socket != null) {
      print('Closing socket');
      socket!.destroy();
    }
    print('Closed');

    connected = false;
    registered = false;

    disconnecting = false;
    await runCallbacks(EventType.DISCONNECT_EVENT, serverData);

    eventListeners.clear();
    eventListenerRegistry.clear();
  }
}
