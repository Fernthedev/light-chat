import 'package:light_chat_core/core.dart';
import 'package:light_chat_core/packet_io.dart';
import 'package:light_chat_core/packets_codecs.dart';
import 'package:light_chat_core/packets.dart';
import 'client.dart';

abstract class PacketListener {
  ///
  /// [result] is the result from [PacketEventHandler]
  ///
  void handle(Packet p, [Object result]);
}

class PacketEventHandler {
  Client client;

  PacketEventHandler(this.client);

  void received(Packet p) async {
    Object result;
    switch (p.runtimeType) {
      case PingReceive:
        client.endTime = DateTime.now();

        client.milliPingDelay =
            client.endTime.difference(client.startTime).inMilliseconds;

        result = client.milliPingDelay;

        print('Ping delay: ${client.milliPingDelay}');
        break;

      case PingPacket:
        client.startTime = DateTime.now();

        await client.send(PongPacket.create(), false);
        break;

      case MessagePacket:
        MessagePacket packet = p;
        print('${packet.message}');
        result = packet.message;
        break;
      case IllegalConnection:
        IllegalConnection packet = p;
        print('Illegal connection error from server: ${packet.message}');
        result = packet.message;

        await client.close();
        break;
      case InitialHandshakePacket:
        InitialHandshakePacket packet = p;

        if (Variables.debug) print('Data of packet: ${p.toJson()}');

        var versionData = VersionData.fromVersionDataString(packet.versionData);
        var range =
            Variables.versionData.getVersionRangeStatusSingle(versionData);

        if (range == VersionRange.MATCH_REQUIREMENTS) {
          print('Version range matches requirements, continuing');
        } else {
          if (range == VersionRange.WE_ARE_LOWER) {
            print(
                "The client version (${Variables.versionData.version}) does not meet server's minimum version (${versionData.minVersion}) requirements. Expect incompatibility issues");
          }

          if (range == VersionRange.WE_ARE_HIGHER) {
            print(
                "The server version (${versionData.version}) does not meet client's minimum version (${Variables.versionData.minVersion}) requirements. Expect incompatibility issues");
          }
        }

        var keyParameter = client.getKey();

        var keyResponsePacket =
            KeyResponsePacket.create(keyParameter.key, packet.publicKeyAsKey);

        await client.send(keyResponsePacket, false);

        result = range;
        break;

      case RequestConnectInfoPacket:
        var connectedPacket = client.connectedPacketInfo;
        await client.send(connectedPacket);
        print('Sent info packet to server');

        result = connectedPacket;
        break;
      case SelfMessagePacket:
        SelfMessagePacket packet = p;
        switch (packet.type) {
          case MessageType.LOST_SERVER_CONNECTION:
            await client.close();
            print('Lost server connection');
            break;
          case MessageType.REGISTER_PACKET:
            client.registered = true;
            print('Sucessfully registered.');
            break;
          case MessageType.TIMED_OUT_REGISTRATION:
            print('The connection timed out');
            break;
          case MessageType.FILL_PASSWORD:
            if (client.serverData.hashedPassword != null &&
                client.serverData.hashedPassword.isNotEmpty) {
              await client.send(HashedPasswordPacket.create(
                  HashedPassword.fromHash(client.serverData.hashedPassword)));
            }
            break;
          default:
            Variables.printDebug('Ignoring self message ${packet.type}');
            break;
        }

        result = packet.type;
        break;
    }

    client.handlePacket(p, result);
  }
}
