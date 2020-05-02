import 'dart:html';

import 'data/packetdata.dart';

class Variables {

  static String defaultLangFramework = "Dart " + 

  static final VersionData versionData =
      VersionData.fromString('1.6.0', '1.6.0');

  static bool _debug = false;

  static bool get debug => _debug;

  static set debug(bool debug) {
    print('Debug mode: $debug');
    _debug = debug;
  }

  static void printDebug(Object o) {
    if (Variables.debug) print(o);
  }

  static String multicastIP;
}
