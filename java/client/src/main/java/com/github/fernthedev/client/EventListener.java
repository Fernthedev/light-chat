package com.github.fernthedev.client;

import com.github.fernthedev.packets.*;
import com.github.fernthedev.universal.EncryptionHandler;
import org.apache.commons.lang3.Validate;

public class EventListener {

    protected Client client;

    public EventListener(Client client) {
        this.client = client;
    }

    public void received(Object p) {
        if(p instanceof SelfMessagePacket) {
            SelfMessagePacket packet = (SelfMessagePacket) p;

            switch (packet.getMessageType()) {
                case LostServerConnectionPacket:
                    client.getLogger().info("Lost connection to server! Must have shutdown!");
                    client.getClientThread().disconnect();
                    break;
                case PingPacket:
                    ClientThread.startTime = System.nanoTime();

                    client.getClientThread().sendObject(SelfMessagePacket.newBuilder().setMessageType(SelfMessageType.PongPacket).build(),false);
                    break;
                case PingReceive:
                    ClientThread.endTime = System.nanoTime();

                    ClientThread.miliPingDelay = ClientThread.endTime - ClientThread.startTime;

                    client.getLogger().debug("Ping: " +(ClientThread.miliPingDelay / 1000000) + "ms");
                    break;
                case RegisterPacket:
                    client.getLogger().info("Successfully connected to server");
                    break;
                case TimedOutRegistrationPacket:
                    client.getLogger().info("Timed out on registering.");
                    client.getClientThread().close();
                    break;
                case DisconnectPacket:
                    client.getClientThread().disconnect();
                    break;
            }
        } else if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;
            client.getLogger().info(messagePacket.getMessage());

        } else if (p instanceof IllegalConnectionPacket) {
            client.getLogger().info(((IllegalConnectionPacket) p).getMessage());
        } else if(p instanceof RequestInfoPacket) {
            RequestInfoPacket packet = (RequestInfoPacket) p;

            client.setServerKey(packet.getEncryptionKey());
            client.getClientThread().setEncryptCipher(client.getClientThread().registerEncryptCipher(client.getServerKey()));

            String pass = EncryptionHandler.makeSHA256Hash(client.getUuid().toString());

            Validate.notNull(pass);

            String privateKey = EncryptionHandler.encrypt(pass,packet.getEncryptionKey());

            client.setPrivateKey(pass);
            client.getClientThread().setDecryptCipher(client.getClientThread().registerDecryptCipher(client.getPrivateKey()));

            client.registered = true;

            ConnectedPacket.Builder connectedPacket = client.getClientThread().getClientHandler().getConnectedPacket();

            connectedPacket.setPrivateKey(privateKey);

            client.getClientThread().sendObject(connectedPacket.build(),false);

            //client.getClientThread().sendObject(connectedPacket);



            client.getLogger().debug("Sent connect packet for request");

        } else if(p instanceof AutoCompletePacket) {
            AutoCompletePacket packet = (AutoCompletePacket) p;

            client.getCompleteHandler().addCandidates(packet.getCandidateListList());
        }
    }

}
