import 'dart:io';
import 'dart:isolate';

import 'package:light_chat_client/EventListener.dart';
import 'package:light_chat_client/client.dart';
import 'package:light_chat_client/data/serverdata.dart';
import 'package:light_chat_client/executor.dart';
import 'package:light_chat_core/core.dart';
import 'package:light_chat_core/packet_io.dart';
import 'package:light_chat_core/packets_codecs.dart';
import 'package:light_chat_core/packets.dart';

void main(List<String> arguments) {
  startClient(arguments);

  // client.onConnect(readCmdLine);
  // client.onConnect(f);
}

