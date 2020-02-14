package com.github.fernthedev.client;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.VersionData;
import com.github.fernthedev.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.core.packets.*;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.core.packets.handshake.InitialHandshakePacket;
import com.github.fernthedev.core.packets.handshake.KeyResponsePacket;
import com.github.fernthedev.core.packets.handshake.RequestConnectInfoPacket;
import com.github.fernthedev.core.packets.latency.PingPacket;
import com.github.fernthedev.core.packets.latency.PingReceive;
import com.github.fernthedev.core.packets.latency.PongPacket;
import com.github.fernthedev.fernutils.thread.ThreadUtils;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.util.concurrent.TimeUnit;

public class EventListener {

    protected Client client;

    public EventListener(Client client) {
        this.client = client;
    }

    public void received(Packet p) {
        if (p instanceof TestConnectPacket) {
            TestConnectPacket packet = (TestConnectPacket) p;
            client.getLogger().info("Connected packet: {}", packet.getMessage());
        } else if (p instanceof PingPacket) {
            client.startPingStopwatch();

            client.sendObject(new PongPacket(), false);
        } else if (p instanceof PingReceive) {

            client.endPingStopwatch();


            client.getLogger().debug("Ping: {}", (client.getPingTime(TimeUnit.MILLISECONDS)) + "ms");

        } else if (p instanceof IllegalConnection) {
            client.getLogger().info(((IllegalConnection) p).getMessage());
        } else if (p instanceof InitialHandshakePacket) {
            // Handles object encryption key sharing
            InitialHandshakePacket packet = (InitialHandshakePacket) p;

            VersionData versionData = packet.getVersionData();

            StaticHandler.VERSION_RANGE versionRange = StaticHandler.getVersionRangeStatus(versionData);

            if (versionRange == StaticHandler.VERSION_RANGE.MATCH_REQUIREMENTS)
                client.getLogger().info("Version range requirements match Server version.");
            else {
                // Current version is smaller than the server's required minimum
                if (versionRange == StaticHandler.VERSION_RANGE.WE_ARE_LOWER) {
                    client.getLogger().info("The client version ({}) does not meet server's minimum version ({}) requirements. Expect incompatibility issues", StaticHandler.getVERSION_DATA().getVersion(), versionData.getMinVersion());
                }

                // Current version is larger than server's minimum version
                if (versionRange == StaticHandler.VERSION_RANGE.WE_ARE_HIGHER) {
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
        } else if (p instanceof RequestConnectInfoPacket) {
            ConnectedPacket connectedPacket = client.getClientHandler().getConnectedPacket();

            client.sendObject(connectedPacket);
            client.getLogger().info("Sent the Connect Packet for request");

        } else if (p instanceof SelfMessagePacket) {
            switch (((SelfMessagePacket) p).getType()) {
                case TIMED_OUT_REGISTRATION:
                    client.getLogger().info("Timed out on registering.");
                    client.close();
                    break;

                case REGISTER_PACKET:
                    client.setRegistered(true);
                    client.getLogger().info("Successfully connected to server");
                    break;

                case LOST_SERVER_CONNECTION:
                    client.getLogger().info("Lost connection to server! Must have shutdown!");
                    try {
                        client.disconnect();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
            }

        }
        ThreadUtils.runForLoopAsync(client.getPacketHandlers(), iPacketHandler -> {
            iPacketHandler.handlePacket(p);
            return null;
        }).runThreads();
    }

}
