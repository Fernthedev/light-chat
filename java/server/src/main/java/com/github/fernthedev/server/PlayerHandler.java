package com.github.fernthedev.server;


import com.github.fernthedev.packets.SelfMessagePacket;
import com.github.fernthedev.packets.SelfMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class PlayerHandler implements Runnable {

    @NonNull
    private Server server;

    public static ConcurrentMap<Integer, ClientPlayer> players = new ConcurrentHashMap<>();


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        Map<ClientPlayer, TimeoutData> clientPlayerMap = new HashMap<>();
        while (server.isRunning()) {
            for (ClientPlayer clientPlayer : Server.socketList.values()) {

                if (!clientPlayerMap.containsKey(clientPlayer)) {
                    clientPlayerMap.put(clientPlayer, new TimeoutData(0, 0));
                }

                TimeoutData timeoutData = clientPlayerMap.get(clientPlayer);

                timeoutData.secondsPassed++;

                if (!clientPlayer.registered || !clientPlayer.channel.isActive()) {
                    timeoutData.registerTimeout++;
                } else timeoutData.registerTimeout = 0;

                if (!clientPlayer.channel.isActive() && timeoutData.registerTimeout >= 30) {
                    clientPlayer.close();
                }


                if (timeoutData.registerTimeout > 30 && !clientPlayer.registered) {
                    clientPlayer.sendObject(SelfMessagePacket.newBuilder().setMessageType(SelfMessageType.TimedOutRegistrationPacket).build(), false);

                    clientPlayer.close();
                }

                if (timeoutData.secondsPassed >= 5) {
                     //clientPlayer.ping();
                    timeoutData.secondsPassed = 0;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Data
    @AllArgsConstructor
    private class TimeoutData {
        private int secondsPassed;
        private int registerTimeout;
    }
}
