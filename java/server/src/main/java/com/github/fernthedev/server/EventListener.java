package com.github.fernthedev.server;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.VersionData;
import com.github.fernthedev.core.data.LightCandidate;
import com.github.fernthedev.core.packets.*;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.core.packets.handshake.KeyResponsePacket;
import com.github.fernthedev.core.packets.handshake.RequestConnectInfoPacket;
import com.github.fernthedev.core.packets.latency.PongPacket;
import com.github.fernthedev.fernutils.threads.ThreadUtils;
import com.github.fernthedev.server.event.chat.ChatEvent;
import org.apache.commons.lang3.StringUtils;

import java.security.InvalidKeyException;
import java.util.List;

public class EventListener {

    private Server server;

    private ClientPlayer clientPlayer;
    
    public EventListener(Server server, ClientPlayer clientPlayer) {
        this.server = server;
        this.clientPlayer = clientPlayer;
    }
    
    public void received(Packet p) {

        //Packet p = (Packet) EncryptionHandler.decrypt(pe, clientPlayer.getServerKey());


        // Server.getLogger().info(clientPlayer + " is the sender of packet");

        if (p instanceof KeyResponsePacket) {
            KeyResponsePacket responsePacket = (KeyResponsePacket) p;
            try {
                clientPlayer.setSecretKey(responsePacket.getSecretKey(clientPlayer.getTempKeyPair().getPrivate()));
                clientPlayer.sendObject(new RequestConnectInfoPacket());
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

        } else if (p instanceof TestConnectPacket) {
            TestConnectPacket packet = (TestConnectPacket) p;
            Server.getLogger().info("Connected packet: {}", packet.getMessage());
        } else if(p instanceof PongPacket) {
            clientPlayer.endTime = System.nanoTime();

            clientPlayer.setDelayTime((clientPlayer.endTime - clientPlayer.startTime) / 1000000);

        } else if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;


            ChatEvent chatEvent = new ChatEvent(clientPlayer, messagePacket.getMessage(),false,true);

            server.getPluginManager().callEvent(chatEvent);

            server.getCommandMessageParser().onCommand(chatEvent);
        } else if (p instanceof CommandPacket) {

            CommandPacket packet = (CommandPacket) p;

            String command = packet.getMessage();

            ChatEvent chatEvent = new ChatEvent(clientPlayer,command,true,true);
            server.getPluginManager().callEvent(chatEvent);

            server.getCommandMessageParser().onCommand(chatEvent);
        }else if (p instanceof AutoCompletePacket) {
            AutoCompletePacket packet = (AutoCompletePacket) p;
            List<LightCandidate> candidates = server.getAutoCompleteHandler().handleLine(packet.getWords());

            packet.setCandidateList(candidates);
            clientPlayer.sendObject(packet);
        } else if (p instanceof HashedPasswordPacket) {
            server.getAuthenticationManager().attemptAuthenticationHash(
                    ((HashedPasswordPacket) p).getHashedPassword(),
                    clientPlayer
            );
        }

        ThreadUtils.runForLoopAsync(server.getPacketHandlers(), iPacketHandler -> {
            iPacketHandler.handlePacket(p, clientPlayer);
            return null;
        }).runThreads();


    }

//    private Object decrypt(SealedObject sealedObject) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
//        SecretKeySpec sks = new SecretKeySpec(clientPlayer.getClientKey().getBytes(), StaticHandler.getCipherTransformation());
//        Cipher cipher = Cipher.getInstance(StaticHandler.getCipherTransformation());
//        cipher.init(Cipher.DECRYPT_MODE, sks);
//
//        try {
//            return sealedObject.getObject(cipher);
//        } catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public void handleConnect(ConnectedPacket packet) {
        //Server.getLogger().info("Connected packet received from " + clientPlayer.getAdress());
        int id = 1;

        if(PlayerHandler.players.size() > 0) {
            /*while(id < PlayerHandler.players.size()) {
                id++;
            }*/
            int lastId = 0;

            for(int i = 0; i < PlayerHandler.players.size();i++) {
                if(PlayerHandler.players.get(i) == null) {
                    id = lastId+1;
                }else{
                    lastId = PlayerHandler.players.get(i).getId();
                }
            }
        }

        if(!isAlphaNumeric(packet.getName())) {
            disconnectIllegalName(packet,"Name requires alphanumeric characters only");
            return;
        }


        for(ClientPlayer player : PlayerHandler.players.values()) {
            if(player.getName().equalsIgnoreCase(packet.getName())) {
                disconnectIllegalName(packet,"Name already in use");
                return;
            }
        }


        /*
        Server.getLogger().debug(server.getBanManager() + " is result");

        if(server.getBanManager().isBanned(clientPlayer)) {
            Server.getLogger().debug("Player is banned " + clientPlayer.channel.remoteAddress());
            clientPlayer.sendObject(new MessagePacket("Your have been banned."),false);
            clientPlayer.close();
            return;
        }*/

//        clientPlayer.setClientUUID(packet.getUuid(),packet.getPublicKey());

//        clientPlayer.setKeyPair(packet.getKeyPair());

        //Server.getLogger().info("Players: " + PlayerHandler.players.size());


        clientPlayer.setDeviceName(packet.getName());
        clientPlayer.setId(id);
        clientPlayer.os = packet.getOS();

        VersionData versionData = packet.getVersionData();

        StaticHandler.VERSION_RANGE versionRange = StaticHandler.getVersionRangeStatus(versionData);

        if (versionRange == StaticHandler.VERSION_RANGE.MATCH_REQUIREMENTS) Server.getLogger().info("{}'s version range requirements match Server version.", clientPlayer);
        else {
            // Current version is larger than client's minimum version
            if(versionRange == StaticHandler.VERSION_RANGE.WE_ARE_HIGHER) {
                Server.getLogger().info("{}'s version ({}) does not meet server's minimum version ({}) requirements. Expect incompatibility issues", clientPlayer, versionData.getVersion(), StaticHandler.getVERSION_DATA().getMinVersion());
            }


            // Current version is smaller than the client's required minimum
            if (versionRange == StaticHandler.VERSION_RANGE.WE_ARE_LOWER) {
                Server.getLogger().info("The server version ({}) does not meet {}'s minimum version ({}) requirements. Expect incompatibility issues", StaticHandler.getVERSION_DATA().getVersion(), clientPlayer, versionData.getMinVersion());
            }
        }


        PlayerHandler.players.put(clientPlayer.getId(), clientPlayer);

      //  Server.getLogger().info("Password required: " + server.getSettingsManager().getSettings().isPasswordRequiredForLogin());

        if(server.getSettingsManager().getConfigData().isPasswordRequiredForLogin()) {
            boolean authenticated = server.getAuthenticationManager().authenticate(clientPlayer);
            if(!authenticated) {
                clientPlayer.sendObject(new MessagePacket("Unable to authenticate"));
                clientPlayer.close();
                return;
            }
        }

        clientPlayer.setRegistered(true);

        Server.getLogger().info("{} has connected to the server [{}]", clientPlayer.getName(), clientPlayer.os);
        Server.broadcast(clientPlayer.getName() + " has joined the server. [" + clientPlayer.os + "]");
        clientPlayer.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.REGISTER_PACKET));
        Server.getLogger().debug("NAME:ID {}:{}", clientPlayer.getName(), clientPlayer.getId());
        Server.getLogger().debug("{} the name.{} the id", PlayerHandler.players.get(clientPlayer.getId()).getName(), PlayerHandler.players.get(clientPlayer.getId()).getId());

    }

    public boolean isAlphaNumeric(String name) {
        return StringUtils.isAlphanumericSpace(name.replace('-',' '));
    }

    private void disconnectIllegalName(ConnectedPacket packet,String message) {
        Server.getLogger().info("{} was disconnected for illegal name. Name: {} Reason: {} ID {}", clientPlayer, packet.getName(), message, clientPlayer.getId());
        clientPlayer.sendObject(new IllegalConnection("You have been disconnected for illegal name. Name: " + packet.getName() + " Reason: " + message),false);
        clientPlayer.close();
    }
}
