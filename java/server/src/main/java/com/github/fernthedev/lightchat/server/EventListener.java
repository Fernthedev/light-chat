package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.VersionData;
import com.github.fernthedev.lightchat.core.exceptions.ParsePacketException;
import com.github.fernthedev.lightchat.core.packets.IllegalConnectionPacket;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.lightchat.core.packets.handshake.KeyResponsePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.RequestConnectInfoPacket;
import com.github.fernthedev.lightchat.core.packets.latency.PingReceive;
import com.github.fernthedev.lightchat.core.packets.latency.PongPacket;
import com.github.fernthedev.lightchat.core.util.ExceptionUtil;
import com.github.fernthedev.lightchat.server.event.PlayerJoinEvent;
import org.apache.commons.lang3.StringUtils;

public class EventListener {

    private final Server server;

    private final ClientConnection clientConnection;

    public EventListener(Server server, ClientConnection clientConnection) {
        this.server = server;
        this.clientConnection = clientConnection;
    }

    public void received(Packet p, int packetId) {

        try {
            //Packet p = (Packet) EncryptionHandler.decrypt(pe, clientConnection.getServerKey());


            // server.logInfo(clientConnection + " is the sender of packet");
            if (p instanceof PongPacket) {
                clientConnection.finishPing();

                clientConnection.sendObject(new PingReceive(), false);

            } else if (p instanceof KeyResponsePacket) {
                StaticHandler.getCore().getLogger().debug("Received key");

                KeyResponsePacket responsePacket = (KeyResponsePacket) p;


                clientConnection.setSecretKey(responsePacket.getSecretKey(clientConnection.getTempKeyPair().getPrivate()));
                clientConnection.sendObject(new RequestConnectInfoPacket());


            }

            ThreadUtils.runForLoopAsync(server.getPacketHandlers(), iPacketHandler -> {
                try {
                    iPacketHandler.handlePacket(p, clientConnection, packetId);
                } catch (Exception e) {
                    throw ExceptionUtil.throwParsePacketException(e, p);
                }
            }).runThreads();

        } catch (ParsePacketException e) {
            throw e;
        } catch (Exception e) {
            throw ExceptionUtil.throwParsePacketException(e, p);
        }
    }

    public void handleConnect(ConnectedPacket packet) {
        try {
            if (!isAlphaNumeric(packet.getName())) {
                disconnectIllegalName(packet, "Name requires alphanumeric characters only. (dashes, periods and @ symbols are allowed)");
                return;
            }


            for (ClientConnection player : server.getPlayerHandler().getUuidMap().values()) {
                if (player.getName().equalsIgnoreCase(packet.getName())) {
                    disconnectIllegalName(packet, "Name already in use");
                    return;
                }
            }

            clientConnection.finishConstruct(packet.getName(), packet.getOS(), packet.getLangFramework());

            VersionData versionData = packet.getVersionData();

            StaticHandler.VersionRange versionRange = StaticHandler.getVersionRangeStatus(versionData);

            if (versionRange == StaticHandler.VersionRange.MATCH_REQUIREMENTS)
                server.logInfo("{}'s version range requirements match Server version.", clientConnection);
            else {
                // The current version is larger than client's minimum version
                if (versionRange == StaticHandler.VersionRange.WE_ARE_HIGHER) {
                    server.logInfo("{}'s version ({}) does not meet server's minimum version ({}) requirements. Expect incompatibility issues", clientConnection, versionData.getVersion(), StaticHandler.getVERSION_DATA().getMinVersion());
                }


                // The current version is smaller than the client's required minimum
                if (versionRange == StaticHandler.VersionRange.WE_ARE_LOWER) {
                    server.logInfo("The server version ({}) does not meet {}'s minimum version ({}) requirements. Expect incompatibility issues", StaticHandler.getVERSION_DATA().getVersion(), clientConnection, versionData.getMinVersion());
                }
            }


            server.getPlayerHandler().getUuidMap().put(clientConnection.getUuid(), clientConnection);

            clientConnection.setRegistered(true);

            server.logInfo("{} has connected to the server [{} | {}] ", clientConnection.getName(), clientConnection.getOs(), clientConnection.getLangFramework());
            clientConnection.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.REGISTER_PACKET));
            ThreadUtils.runAsync(() -> server.getPluginManager().callEvent(new PlayerJoinEvent(clientConnection, true)));
        } catch (Exception e) {
            throw ExceptionUtil.throwParsePacketException(e, packet);
        }
    }

    public boolean isAlphaNumeric(String name) {
        return StringUtils.isAlphanumericSpace(name.replace('-', ' ')
                .replace(".", " ")
                .replace("'"," ")
                .replace("@"," "));
    }

    private void disconnectIllegalName(ConnectedPacket packet, String message) {
        server.logInfo("{} was disconnected for illegal name. Name: {} Reason: {} ID {}", clientConnection, packet.getName(), message, clientConnection.getUuid().getMostSignificantBits());
        clientConnection.sendObject(new IllegalConnectionPacket("You have been disconnected for an illegal name. Name: " + packet.getName() + " Reason: " + message), false);
        clientConnection.close();
    }
}
