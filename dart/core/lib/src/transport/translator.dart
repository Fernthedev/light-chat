import 'dart:collection';

import 'dart:ffi';

/// This translates classes to

/// strings that can be translated back

/// to classes in the client

/// to avoid using java class names.

class ClassTranslator {
  static final Map<Type, String> _classTranslateMap = HashMap();
  static final Map<String, Type> _reverseTranslationMap = HashMap();

  static bool _initialized = false;

  static void init() {
    if (!_initialized) {
      _registerTranslations();
      _registerReverseTranslations();
      _initialized = true;
    }
  }

  static void _registerTranslations() {
    registerTranslation(String, 'string');
    registerTranslation(BigInt, 'big_integer');
    registerTranslation(double, 'double');
    registerTranslation(Float, 'float');
    registerTranslation(Int8, 'byte');
    registerTranslation(Uint8, 'byte');
    registerTranslation(Int16, 'short');
    registerTranslation(Int32, 'int');
    registerTranslation(Int64, 'long');
    registerTranslation(int, 'long');
  }

  static void _registerReverseTranslations() {
    registerReverseTranslation(String, 'string');
    registerReverseTranslation(BigInt, 'big_integer');
    registerReverseTranslation(double, 'double');
    registerReverseTranslation(Int8, 'byte');
    registerReverseTranslation(Int16, 'short');
    registerReverseTranslation(Int32, 'int');
    registerReverseTranslation(Int64, 'long');
    registerReverseTranslation(int, 'long');
    registerReverseTranslation(Float, 'float');
  }

  static void registerTranslation(Type clazz, String translate) {
    _classTranslateMap[clazz] = translate;
  }

  static void registerReverseTranslation(Type clazz, String translate) {
    _reverseTranslationMap[translate] = clazz;
  }

  static String translate(Type clazz) {
    return _classTranslateMap[clazz];
  }

  static Type findTranslation(String lookup) {
    return _reverseTranslationMap[lookup];
  }
}
