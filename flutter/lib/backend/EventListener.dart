
import 'package:lightchat_client/assets/variables.dart';
import 'package:lightchat_client/backend/client.dart';
import 'package:lightchat_client/backend/packets/handshake_packets.dart';
import 'package:lightchat_client/backend/packets/latency_packets.dart';
import 'package:lightchat_client/backend/packets/other_packets.dart';
import 'package:lightchat_client/backend/packets/packets.dart';
import 'package:lightchat_client/data/packetdata.dart';
import 'package:pointycastle/api.dart';

abstract class PacketListener {
  ///
  /// [result] is the result from [PacketEventHandler]
  ///
  handle(Packet p, [Object result]);
}

class PacketEventHandler {
  Client client;

  PacketEventHandler(this.client);

  void received(Packet p) async {
    Object result;
    switch (p.runtimeType) {
      case PingPacket:
        client.startTime = DateTime.now();

        client.send(PongPacket(), false);
        break;

      case PingReceive:
        client.endTime = DateTime.now();

        client.milliPingDelay = client.endTime.difference(client.startTime).inMilliseconds;

        result = client.milliPingDelay;

        print("Ping delay: ${client.milliPingDelay}");
        break;
      case MessagePacket:
        MessagePacket packet = p;
        print("${packet.message}");
        result = packet.message;
        break;
      case IllegalConnection:
        IllegalConnection packet = p;
        print("Illegal connection error from server: ${packet.message}");
        result = packet.message;

        client.close();
        break;
      case InitialHandshakePacket:
        InitialHandshakePacket packet = p;

        print("Data of packet: ${p.toJson()}");

        VersionData versionData = VersionData.fromVersionDataString(packet.versionData);
        VersionRange range = Variables.getVersionRangeStatusSingle(versionData);

        if (range == VersionRange.MATCH_REQUIREMENTS) {
          print("Version range matches requirements, continuing");
        } else {
          if(range == VersionRange.WE_ARE_LOWER) {
            print(
                "The client version (${Variables.versionData.version}) does not meet server's minimum version (${versionData.minVersion}) requirements. Expect incompatibility issues");
          }

          if (range == VersionRange.WE_ARE_HIGHER) {
            print("The server version (${versionData.version}) does not meet client's minimum version (${Variables.versionData.minVersion}) requirements. Expect incompatibility issues");
          }
        }

        KeyParameter keyParameter = client.getKey();

        KeyResponsePacket keyResponsePacket = KeyResponsePacket.create(keyParameter.key, packet.publicKeyAsKey);

        client.send(keyResponsePacket, false);

        result = range;
        break;

      case RequestConnectInfoPacket:
        ConnectedPacket connectedPacket = client.connectedPacketInfo;
        client.send(connectedPacket);
        print("Sent info packet to server");

        result = connectedPacket;
        break;
      case SelfMessagePacket:
        SelfMessagePacket packet = p;
        switch(packet.messageType) {
          case MessageType.LOST_SERVER_CONNECTION:
            client.close();
            print("Lost server connection");
            break;
          case MessageType.REGISTER_PACKET:
            client.registered = true;
            print("Sucessfully registered.");
            break;
          case MessageType.TIMED_OUT_REGISTRATION:
            print("The connection timed out");
            break;
          case MessageType.FILL_PASSWORD:
            break;
        }

        result = packet.messageType;
        break;
    }

    client.handlePacket(p, result);
  }
}