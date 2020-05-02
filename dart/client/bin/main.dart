import 'dart:io';

import 'package:light_chat_client/EventListener.dart';
import 'package:light_chat_client/client.dart';
import 'package:light_chat_client/data/packetdata.dart';
import 'package:light_chat_client/data/serverdata.dart';
import 'package:light_chat_client/packets/handshake_packets.dart';
import 'package:light_chat_client/packets/other_packets.dart';
import 'package:light_chat_client/packets/packets.dart';
import 'package:light_chat_client/variables.dart';

void main(List<String> arguments) {
  String host;
  var port = -1;
  String name;

  for (var arg in arguments) {
    if (Variables.debug) {
      print('Checking arg: $arg');
    }

    if (arg == '-ip' || arg == '-host') {
      host = getValueFromArg(arg, arguments);
    }

    if (arg == '-port') {
      port = int.parse(getValueFromArg(arg, arguments));
    }

    if (arg == '-name') {
      name = getValueFromArg(arg, arguments);
    }

    if (arg == '-debug') {
      Variables.debug = true;
    }
  }

  host = setValIfNull(host, 'Host:');
  port = int.parse(setValIfNull(port.toString(), 'Port:'));
  name = setValIfNull(name, 'Name:');

  client = Client(ConnectedPacket.create(
      name, Platform.operatingSystem, Variables.versionData, Variables.defaultLangFramework));

  client.addPacketListener(PacketListenerConsole());

  client.initializeConnection(ServerData(host, port, null)).then((f) {
    print('Initialized connection sucessfully');
  });

  client.onDisconnect((s) {
    exit(0);
  });

  // client.onConnect(readCmdLine);
  // client.onConnect(f);
}

Client client;

class PacketListenerConsole extends PacketListener {
  @override
  void handle(Packet p, [Object result]) {
    // print('Handling packet ${p.runtimeType}');
    switch (p.runtimeType) {
      case SelfMessagePacket:
        SelfMessagePacket packet = p;
        // print('Message Type: ${p.toString()}');
        switch (packet.type) {
          case MessageType.INCORRECT_PASSWORD_ATTEMPT:
          case MessageType.FILL_PASSWORD:
            print('Reading console input');
            var line = stdin.readLineSync();

            print('Sent the password $line');

            var hashedPasswordPacket =
                HashedPasswordPacket.create(HashedPassword(line));

            client.send(hashedPasswordPacket);
            break;
          case MessageType.LOST_SERVER_CONNECTION:
            break;
          case MessageType.REGISTER_PACKET:
            break;
          case MessageType.TIMED_OUT_REGISTRATION:
            break;
          case MessageType.INCORRECT_PASSWORD_FAILURE:
            print('Unable to authenticate with password');
            break;
        }
        break;
    }
  }
}

// TODO: Make async to allow printing to console while reading input.
void readCmdLine() async {
  print('Checking read');
  // return;
  while (client.connected) {
    print('Reading');
    var line = stdin.readLineSync().trim().replaceAll('  ', ' ');
    print('Read $line');

    Packet messagePacket = MessagePacket.create(line);

    if (line.startsWith('/')) {
      line = line.substring(line.indexOf('/'));
      messagePacket = CommandPacket.create(line);
    }

    if (line == '' || line.isEmpty) continue;

    await client.send(messagePacket);
  }
}

String setValIfNull(String arg, String message) {
  if (arg == null || arg == '-1') {
    print(message);
    return stdin.readLineSync();
  } else {
    return arg;
  }
}

String getValueFromArg(String arg, List<String> arguments) {
  var index = arguments.indexOf(arg);

  if (arguments.length >= index + 1) {
    return arguments[index + 1];
  } else {
    throw IndexError(index + 1, arguments, 'Not enough arguments provided');
  }
}
