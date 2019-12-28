
import 'dart:collection';

import 'package:chatclientflutter/backend/packets/packets.dart';

class PacketRegistry {

  _PacketRegistry() {}

  static const Map<String, Packet> _PACKET_REGISTRY = {};

  static Packet getPacketInstanceFromRegistry(String name, Map<String, dynamic> json) {
    if (!_PACKET_REGISTRY.containsKey(name)) throw new Exception("The packet registry does not contain packet \"" + name + "\" in the registry. Make sure it is spelled correctly and is case-sensitive.");

    return _PACKET_REGISTRY[name].fromJson(json);
  }

  static Type registerPacket(Packet packet) {
    if(_PACKET_REGISTRY.containsKey(packet.packetIdentifier) && _PACKET_REGISTRY[packet.packetIdentifier] != packet) throw ("The packet ${packet.runtimeType} tried to use packet name \"${packet.packetIdentifier}\" which is already taken by the packet ${_PACKET_REGISTRY[packet.packetIdentifier].runtimeType}");

    _PACKET_REGISTRY[packet.packetIdentifier] = packet;

    return packet.runtimeType;
  }



  static RegisteredReturnValues checkIfRegistered(Packet packet) {
    if (!_PACKET_REGISTRY.containsKey(packet.packetIdentifier)) return RegisteredReturnValues.NOT_IN_REGISTRY;

    return _PACKET_REGISTRY[packet.packetIdentifier] == packet.runtimeType ? RegisteredReturnValues.IN_REGISTRY : RegisteredReturnValues.IN_REGISTRY_DIFFERENT_PACKET;
  }

  static void registerDefaultPackets() {

//    for (Package packageT : Package.getPackages()) {
//    if (packageT.getName().startsWith(StaticHandler.PACKET_PACKAGE)) {
//    StaticHandler.getCore().getLogger().debug("Registering the package " + packageT.getName());
//    registerPacketPackage(packageT.getName());
//    }
//    }
  
  }

//  static void registerPacketPackage(String packageName) {
//    Set<Class<? extends Packet>> classes = new Reflections(packageName).getSubTypesOf(Packet.class);
//
//    for (Class<? extends Packet> packetClass : classes) {
//    StaticHandler.getCore().getLogger().debug("Registering the class {}", packetClass);
//
//    try {
//    registerPacket(packetClass);
//    } catch (Exception e) {
//    e.printStackTrace();
//    }
//    }
//  }


}

enum RegisteredReturnValues {
  NOT_IN_REGISTRY,
  IN_REGISTRY,
  IN_REGISTRY_DIFFERENT_PACKET
}