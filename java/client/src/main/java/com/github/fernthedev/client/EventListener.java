package com.github.fernthedev.client;

import com.github.fernthedev.packets.*;
import com.github.fernthedev.packets.latency.PingPacket;
import com.github.fernthedev.packets.latency.PingReceive;
import com.github.fernthedev.packets.latency.PongPacket;
import com.github.fernthedev.universal.EncryptionHandler;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.TimeUnit;

public class EventListener {

    protected Client client;

    public EventListener(Client client) {
        this.client = client;
    }

    public void received(Packet p) {


        if(p instanceof TestConnectPacket) {
            TestConnectPacket packet = (TestConnectPacket) p;
            Client.getLogger().info("Connected packet: " + packet.getMessage());
        } else if(p instanceof LostServerConnectionPacket) {
            LostServerConnectionPacket packet = (LostServerConnectionPacket)p;
            Client.getLogger().info("Lost connection to server! Must have shutdown!");
            client.getClientThread().disconnect();
        }else if(p instanceof PingPacket) {
            ClientThread.startTime = System.nanoTime();

            client.getClientThread().sendObject(new PongPacket(),false);
        } else if(p instanceof PingReceive) {

            ClientThread.endTime = System.nanoTime();

            ClientThread.miliPingDelay = ClientThread.endTime - ClientThread.startTime;

            Client.getLogger().debug("Ping: " + TimeUnit.NANOSECONDS.toMillis(ClientThread.miliPingDelay) + "ms");

        } else if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;
            Client.getLogger().info(messagePacket.getMessage());
        } else if (p instanceof IllegalConnection) {
            Client.getLogger().info(((IllegalConnection) p).getMessage());
        } else if (p instanceof RegisterPacket) {
            client.registered = true;
            Client.getLogger().info("Successfully connected to server");
        }else if(p instanceof RequestInfoPacket) {
            RequestInfoPacket packet = (RequestInfoPacket) p;

            client.setServerKey(packet.getKey());

            String pass = EncryptionHandler.makeSHA256Hash(client.getUuid().toString());

            Validate.notNull(pass);

            String privateKey = EncryptionHandler.encrypt(pass,packet.getKey());

            client.setPrivateKey(pass);

            client.registered = true;

            ConnectedPacket connectedPacket = client.getClientThread().getClientHandler().getConnectedPacket();

            connectedPacket.setPrivateKey(privateKey);

            client.getClientThread().sendObject(connectedPacket,false);

            //client.getClientThread().sendObject(connectedPacket);



            Client.getLogger().debug("Sent connect packet for request");

        }
    }

}
