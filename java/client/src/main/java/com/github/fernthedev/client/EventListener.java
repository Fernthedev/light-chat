package com.github.fernthedev.client;

import com.github.fernthedev.packets.*;
import com.github.fernthedev.packets.latency.PingPacket;
import com.github.fernthedev.packets.latency.PingReceive;
import com.github.fernthedev.packets.latency.PongPacket;
import com.github.fernthedev.universal.EncryptionHandler;
import org.apache.commons.lang3.Validate;

public class EventListener {

    protected Client client;

    public EventListener(Client client) {
        this.client = client;
    }

    public void received(Packet p) {
        if(p instanceof TestConnectPacket) {
            TestConnectPacket packet = (TestConnectPacket) p;
            client.getLogger().info("Connected packet: " + packet.getMessage());
        } else if(p instanceof PingPacket) {
            Client.startTime = System.nanoTime();

            client.sendObject(new PongPacket(),false);
        } else if(p instanceof PingReceive) {

            Client.endTime = System.nanoTime();

            Client.miliPingDelay = Client.endTime - Client.startTime;

            client.getLogger().debug("Ping: " + (Client.miliPingDelay / 1000000) + "ms");

        } else if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;
            client.getLogger().info(messagePacket.getMessage());


        } else if (p instanceof IllegalConnection) {
            client.getLogger().info(((IllegalConnection) p).getMessage());
        } else if(p instanceof RequestInfoPacket) {
            RequestInfoPacket packet = (RequestInfoPacket) p;

            client.setServerKey(packet.getKey());
            client.setEncryptCipher(client.registerEncryptCipher(client.getServerKey()));

            String pass = EncryptionHandler.makeSHA256Hash(client.getUuid().toString());

            Validate.notNull(pass);

            String privateKey = EncryptionHandler.encrypt(pass, packet.getKey());

            client.setPrivateKey(pass);
            client.setDecryptCipher(client.registerDecryptCipher(client.getPrivateKey()));

            client.registered = true;

            ConnectedPacket connectedPacket = client.getClientHandler().getConnectedPacket();

            connectedPacket.setPrivateKey(privateKey);

            client.sendObject(connectedPacket,false);

            //client.sendObject(connectedPacket);



            client.getLogger().info("Sent connect packet for request");

        }else if(p instanceof SelfMessagePacket) {
            switch (((SelfMessagePacket) p).getType()) {
                case TIMED_OUT_REGISTRATION:
                    client.getLogger().info("Timed out on registering.");
                    client.close();
                    break;

                case REGISTER_PACKET:
                    client.registered = true;
                    client.getLogger().info("Successfully connected to server");
                    break;

                case LOST_SERVER_CONNECTION:
                    client.getLogger().info("Lost connection to server! Must have shutdown!");
                    client.disconnect();
                    break;
            }

        } else if(p instanceof AutoCompletePacket) {
            AutoCompletePacket packet = (AutoCompletePacket) p;
            client.getCompleteHandler().addCandidates(packet.getCandidateList());
        }
    }

}
