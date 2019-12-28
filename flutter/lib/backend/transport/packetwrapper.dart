import 'dart:core';

import 'package:chatclientflutter/backend/packets/packets.dart';
import 'package:chatclientflutter/backend/transport/packet_registry.dart';
import 'package:flutter/foundation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:lombok/lombok.dart';

import 'codecs/AcceptablePacketTypes.dart';

//class PacketWrapper<T> {
//
//  bool _ENCRYPT = false;
//
//  @getter
//  T _jsonObject;
//
//  String _aClass;
//
//  @override
//  String toString() {
//    return "PacketWrapper{" +
//        ", aClass='" + _aClass + '\'' +
//        '}';
//  }
//
//  PacketWrapper(T object, Type aClass) {
//  this._aClass = aClass.runtimeType;
//  this._jsonObject = object;
//  }
//
////    @Deprecated
////    public PacketWrapper(@NonNull Object object) {
////        this.jsonObject = gson.toJson(object);
////        this.aClass = object.getClass().getName();
////    }
//
//  bool encrypt() {
//    return _ENCRYPT;
//  }
//
//
//
//  _PacketWrapper() {}
//}
//
//class something<T> {
//  bool _ENCRYPT = false;
//
//  @getter
//  T _jsonObject;
//
//  String _aClass;
//
//
//}

part 'packetwrapper.g.dart';

@JsonSerializable(explicitToJson:true, createFactory: false)

abstract class PacketWrapper<T> {



  @JsonKey(name: "ENCRYPT")
  bool encrypt;

  @getter
  final dynamic jsonObject;

  @getter
  final String packetIdentifier;


  @override
  String toString() {
    return "PacketWrapper{" +
        ", aClass='" + packetIdentifier + '\'' +
        '}';
  }

  PacketWrapper(this.jsonObject, this.packetIdentifier) {
    encrypt = false;
  }

  static String getPacketIdentifier() {
    return "packetIdentifier";
  }

  static String getJsonIdentifier() {
    return "jsonObject";
  }



//    @Deprecated
//    public PacketWrapper(@NonNull Object object) {
//        this.jsonObject = gson.toJson(object);
//        this.aClass = object.getClass().getName();
//    }



//  Map<String, dynamic> toJson() {
//    return {
//      "_ENCRYPT": this._ENCRYPT,
//      "_jsonObject": this._jsonObject,
//      "_packetIdentifier": this._packetIdentifier,
//    };
//  }
//
//  factory PacketWrapper.fromJson(Map<String, dynamic> json) {
//
//    return PacketWrapper.full(encrypt: json["encrypt"].toLowerCase() == 'true',
//      jsonObject: T.fromJson(json["jsonObject"]),
//      packetIdentifier: json["packetIdentifier"],);
//  }


}

abstract class JsonSerializableClass {
  JsonSerializableClass fromJson(Map<String, dynamic> json);

  Map<String, dynamic> toJson();
}

/**
 * Wraps a packet not meant to be encrypted
 */
@JsonSerializable()
class UnencryptedPacketWrapper extends PacketWrapper<Map<String, dynamic>> implements AcceptablePacketTypes {

  @protected
  _UnencryptedPacketWrapper() {}

  UnencryptedPacketWrapper(Packet jsonObject) : super(jsonObject.toJson(), jsonObject.packetIdentifier) {

    if (PacketRegistry.checkIfRegistered(jsonObject) == RegisteredReturnValues.NOT_IN_REGISTRY) {
      throw ("The packet trying to be wrapped is not registered. \"${jsonObject.runtimeType}\"");
    }

    encrypt = false;
  }


}