package com.github.fernthedev.lightchat.client;

import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.lightchat.client.event.ServerConnectFinishEvent;
import com.github.fernthedev.lightchat.client.event.ServerConnectHandshakeEvent;
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.VersionData;
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.lightchat.core.exceptions.ParsePacketException;
import com.github.fernthedev.lightchat.core.packets.IllegalConnectionPacket;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.KeyResponsePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.RequestConnectInfoPacket;
import com.github.fernthedev.lightchat.core.packets.latency.PingPacket;
import com.github.fernthedev.lightchat.core.packets.latency.PingReceive;
import com.github.fernthedev.lightchat.core.packets.latency.PongPacket;
import com.github.fernthedev.lightchat.core.util.ExceptionUtil;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.util.concurrent.TimeUnit;

public class EventListener {

    protected Client client;

    public EventListener(Client client) {
        this.client = client;
    }

    public void received(Packet p, int packetId) {
        try {
            if (p instanceof PingPacket) {
                client.startPingStopwatch();

                client.sendObject(new PongPacket(), false);
            } else if (p instanceof PingReceive) {

                client.endPingStopwatch();


                client.getLogger().debug("Ping: {}", (client.getPingTime(TimeUnit.MILLISECONDS)) + "ms");

            } else if (p instanceof IllegalConnectionPacket) {
                client.getLogger().info(((IllegalConnectionPacket) p).getMessage());
                client.disconnect(ServerDisconnectEvent.DisconnectStatus.CONNECTION_LOST);
            } else if (p instanceof InitialHandshakePacket) {
                // Handles object encryption key sharing
                InitialHandshakePacket packet = (InitialHandshakePacket) p;

                VersionData versionData = packet.getVersionData();

                StaticHandler.VersionRange versionRange = StaticHandler.getVersionRangeStatus(versionData);

                if (versionRange == StaticHandler.VersionRange.MATCH_REQUIREMENTS)
                    client.getLogger().info("Version range requirements match Server version.");
                else {
                    // Current version is smaller than the server's required minimum
                    if (versionRange == StaticHandler.VersionRange.WE_ARE_LOWER) {
                        client.getLogger().info("The client version ({}) does not meet server's minimum version ({}) requirements. Expect incompatibility issues", StaticHandler.getVERSION_DATA().getVersion(), versionData.getMinVersion());
                    }

                    // Current version is larger than server's minimum version
                    if (versionRange == StaticHandler.VersionRange.WE_ARE_HIGHER) {
                        client.getLogger().info("The server version ({}) does not meet client's minimum version ({}) requirements. Expect incompatibility issues", versionData.getVersion(), StaticHandler.getVERSION_DATA().getMinVersion());
                    }

                }

                SecretKey secretKey = EncryptionUtil.generateSecretKey();
                client.setSecretKey(secretKey);


                try {
                    KeyResponsePacket responsePacket = new KeyResponsePacket(secretKey, packet.getPublicKey());

                    client.sendObject(responsePacket, false);
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }

                client.getPluginManager().callEvent(new ServerConnectHandshakeEvent(client.getChannel()));
            } else if (p instanceof RequestConnectInfoPacket) {
                ConnectedPacket connectedPacket = client.buildConnectedPacket();

                client.sendObject(connectedPacket);
                client.getLogger().info("Sent the connection Packet for request");

            } else if (p instanceof SelfMessagePacket) {
                switch (((SelfMessagePacket) p).getType()) {
                    case TIMED_OUT_REGISTRATION:
                        client.getLogger().info("Timed out on registering.");
                        client.disconnect(ServerDisconnectEvent.DisconnectStatus.TIMEOUT);
                        break;

                    case REGISTER_PACKET:
                        client.setRegistered(true);
                        client.getLogger().info("Successfully connected to server");
                        client.getPluginManager().callEvent(new ServerConnectFinishEvent(client.getChannel()));
                        break;

                    case LOST_SERVER_CONNECTION:
                        client.getLogger().info("Lost connection to server! Must have shutdown!");
                        client.disconnect(ServerDisconnectEvent.DisconnectStatus.CONNECTION_LOST);
                        break;
                }

            }
            ThreadUtils.runForLoopAsync(client.getPacketHandlers(), iPacketHandler -> {
                try {
                    iPacketHandler.handlePacket(p, packetId);
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

}
