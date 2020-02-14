package com.github.fernthedev.server;

import com.github.fernthedev.core.packets.SelfMessagePacket;
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
    private static final Map<Channel, ClientPlayer> channelMap = new ConcurrentHashMap<>();

    @NonNull
    private Server server;

    @Getter
    private static final Map<UUID, ClientPlayer> uuidMap = new ConcurrentHashMap<>();


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
        Map<ClientPlayer, TimeoutData> clientPlayerMap = new ConcurrentHashMap<>();
        while (server.isRunning()) {
            for (ClientPlayer clientPlayer : channelMap.values()) {

                if (!clientPlayerMap.containsKey(clientPlayer)) {
                    clientPlayerMap.put(clientPlayer, new TimeoutData(0, 0));
                }

                TimeoutData timeoutData = clientPlayerMap.get(clientPlayer);

                timeoutData.secondsPassed++;

                if (!clientPlayer.isRegistered() || !clientPlayer.getChannel().isActive()) timeoutData.registerTimeout++;
                else timeoutData.registerTimeout = 0;

                if (timeoutData.registerTimeout >= server.getSettingsManager().getConfigData().getTimeoutTime() / 1000 && !clientPlayer.isRegistered()) {
                    clientPlayer.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.TIMED_OUT_REGISTRATION), false);

                    clientPlayer.close();
                }

                if (!clientPlayer.getChannel().isActive() && timeoutData.registerTimeout >= 30) clientPlayer.close();



                if (timeoutData.secondsPassed >= 5) {
                    clientPlayer.ping();
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
