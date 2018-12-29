package com.github.fernthedev.client;

import com.github.fernthedev.packets.*;
import com.github.fernthedev.packets.latency.PingPacket;
import com.github.fernthedev.packets.latency.PingReceive;
import com.github.fernthedev.packets.latency.PongPacket;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class EventListener {

    protected Client client;

    public EventListener(Client client) {
        this.client = client;
    }

    public void recieved(Object p) {
        if(p instanceof TestConnectPacket) {
            TestConnectPacket packet = (TestConnectPacket) p;
            Client.getLogger().info("Connected packet: " + packet.getMessage());
        } else if(p instanceof LostServerConnectionPacket) {
            LostServerConnectionPacket packet = (LostServerConnectionPacket)p;
            Client.getLogger().info("Lost connection to server! Must have shutdown!");
            client.getClientThread().disconnect();
        }else if(p instanceof PingPacket) {
            ClientThread.startTime = System.nanoTime();

            client.getClientThread().sendObject(new PongPacket());
        } else if(p instanceof PingReceive) {

            ClientThread.endTime = System.nanoTime();

            ClientThread.miliPingDelay = ClientThread.endTime - ClientThread.startTime;

            Client.getLogger().log(Level.FINE,"Ping: " + TimeUnit.NANOSECONDS.toMillis(ClientThread.miliPingDelay) + "ms");

        } else if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;
            Client.getLogger().info(messagePacket.toString());
        } else if (p instanceof IllegalConnection) {
            Client.getLogger().info(((IllegalConnection) p).getMessage());
        } else if (p instanceof RegisterPacket) {
            client.registered = true;
            Client.getLogger().info("Successfully connected to server");
        }
    }

}
