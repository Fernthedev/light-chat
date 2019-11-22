import 'dart:core';

import 'package:lombok/lombok.dart';

class PacketWrapper<T> {

  bool _ENCRYPT = false;

  @getter
  T _jsonObject;

  String _aClass;

  @override
  String toString() {
    return "PacketWrapper{" +
        ", aClass='" + _aClass + '\'' +
        '}';
  }

  PacketWrapper(T object, Type aClass) {
  this._aClass = aClass.runtimeType;
  this._jsonObject = object;
  }

//    @Deprecated
//    public PacketWrapper(@NonNull Object object) {
//        this.jsonObject = gson.toJson(object);
//        this.aClass = object.getClass().getName();
//    }

  bool encrypt() {
    return _ENCRYPT;
  }



  _PacketWrapper() {}
}

class something<T> {
  bool _ENCRYPT = false;

  @getter
  T _jsonObject;

  String _aClass;


}