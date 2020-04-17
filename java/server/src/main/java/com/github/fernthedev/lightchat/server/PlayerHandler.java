package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import io.netty.channel.Channel;
import lombok.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles players
 */
@RequiredArgsConstructor
public class PlayerHandler implements Runnable {

    @Getter
    private final Map<Channel, ClientConnection> channelMap = new ConcurrentHashMap<>();

    @NonNull
    private Server server;

    @Getter
    private final Map<UUID, ClientConnection> uuidMap = new ConcurrentHashMap<>();


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
        Map<ClientConnection, TimeoutData> clientPlayerMap = new ConcurrentHashMap<>();
        while (server.isRunning()) {
            for (ClientConnection clientConnection : channelMap.values()) {

                if (!clientPlayerMap.containsKey(clientConnection)) {
                    clientPlayerMap.put(clientConnection, new TimeoutData(0, 0));
                }

                TimeoutData timeoutData = clientPlayerMap.get(clientConnection);

                timeoutData.secondsPassed++;

                if (!clientConnection.isRegistered() || !clientConnection.getChannel().isActive()) timeoutData.registerTimeout++;
                else timeoutData.registerTimeout = 0;

                if (timeoutData.registerTimeout >= server.getSettingsManager().getConfigData().getTimeoutTime() / 1000 && !clientConnection.isRegistered()) {
                    clientConnection.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.TIMED_OUT_REGISTRATION), false);

                    clientConnection.close();
                }

                if (!clientConnection.getChannel().isActive() && timeoutData.registerTimeout >= 30) clientConnection.close();



                if (timeoutData.secondsPassed >= 5) {
                    clientConnection.ping();
                    timeoutData.secondsPassed = 0;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }


    @Data
    @AllArgsConstructor
    private static class TimeoutData {
        private int secondsPassed;
        private int registerTimeout;
    }
}
