package com.github.fernthedev.server;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.VersionData;
import com.github.fernthedev.core.packets.IllegalConnection;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.SelfMessagePacket;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.core.packets.handshake.KeyResponsePacket;
import com.github.fernthedev.core.packets.handshake.RequestConnectInfoPacket;
import com.github.fernthedev.core.packets.latency.PongPacket;
import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.server.event.PlayerJoinEvent;
import org.apache.commons.lang3.StringUtils;

import java.security.InvalidKeyException;

public class EventListener {

    private Server server;

    private ClientConnection clientConnection;
    
    public EventListener(Server server, ClientConnection clientConnection) {
        this.server = server;
        this.clientConnection = clientConnection;
    }
    
    public void received(Packet p, int packetId) {

        //Packet p = (Packet) EncryptionHandler.decrypt(pe, clientConnection.getServerKey());


        // server.logInfo(clientConnection + " is the sender of packet");
        if(p instanceof PongPacket) {
            clientConnection.finishPing();

        } else if (p instanceof KeyResponsePacket) {
            KeyResponsePacket responsePacket = (KeyResponsePacket) p;
            try {
                clientConnection.setSecretKey(responsePacket.getSecretKey(clientConnection.getTempKeyPair().getPrivate()));
                clientConnection.sendObject(new RequestConnectInfoPacket());
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

        }

        ThreadUtils.runForLoopAsync(server.getPacketHandlers(), iPacketHandler -> {
            iPacketHandler.handlePacket(p, clientConnection, packetId);
        }).runThreads();


    }

    public void handleConnect(ConnectedPacket packet) {
        if(!isAlphaNumeric(packet.getName())) {
            disconnectIllegalName(packet,"Name requires alphanumeric characters only");
            return;
        }


        for(ClientConnection player : server.getPlayerHandler().getUuidMap().values()) {
            if(player.getName().equalsIgnoreCase(packet.getName())) {
                disconnectIllegalName(packet,"Name already in use");
                return;
            }
        }

        clientConnection.finishConstruct(packet.getName(), packet.getOS());

        VersionData versionData = packet.getVersionData();

        StaticHandler.VERSION_RANGE versionRange = StaticHandler.getVersionRangeStatus(versionData);

        if (versionRange == StaticHandler.VERSION_RANGE.MATCH_REQUIREMENTS) server.logInfo("{}'s version range requirements match Server version.", clientConnection);
        else {
            // The current version is larger than client's minimum version
            if(versionRange == StaticHandler.VERSION_RANGE.WE_ARE_HIGHER) {
                server.logInfo("{}'s version ({}) does not meet server's minimum version ({}) requirements. Expect incompatibility issues", clientConnection, versionData.getVersion(), StaticHandler.getVERSION_DATA().getMinVersion());
            }


            // The current version is smaller than the client's required minimum
            if (versionRange == StaticHandler.VERSION_RANGE.WE_ARE_LOWER) {
                server.logInfo("The server version ({}) does not meet {}'s minimum version ({}) requirements. Expect incompatibility issues", StaticHandler.getVERSION_DATA().getVersion(), clientConnection, versionData.getMinVersion());
            }
        }


        server.getPlayerHandler().getUuidMap().put(clientConnection.getUuid(), clientConnection);

        clientConnection.setRegistered(true);

        server.logInfo("{} has connected to the server [{}]", clientConnection.getName(), clientConnection.getOs());
        clientConnection.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.REGISTER_PACKET));
        ThreadUtils.runAsync(() -> server.getPluginManager().callEvent(new PlayerJoinEvent(clientConnection, true)));

    }

    public boolean isAlphaNumeric(String name) {
        return StringUtils.isAlphanumericSpace(name.replace('-',' '));
    }

    private void disconnectIllegalName(ConnectedPacket packet,String message) {
        server.logInfo("{} was disconnected for illegal name. Name: {} Reason: {} ID {}", clientConnection, packet.getName(), message, clientConnection.getUuid().getMostSignificantBits());
        clientConnection.sendObject(new IllegalConnection("You have been disconnected for an illegal name. Name: " + packet.getName() + " Reason: " + message),false);
        clientConnection.close();
    }
}
