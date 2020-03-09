package com.github.fernthedev.server;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.VersionData;
import com.github.fernthedev.core.api.APIUsage;
import com.github.fernthedev.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.core.encryption.util.RSAEncryptionUtil;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.latency.PingPacket;
import com.github.fernthedev.fernutils.thread.InterfaceTaskInfo;
import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.fernutils.thread.functional.Task;
import com.github.fernthedev.fernutils.thread.single.TaskInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.*;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Handles client data
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClientConnection implements SenderInterface, AutoCloseable {

    private final Server server;

    @Getter
    private boolean connected;

    @Getter
    @Setter
    private boolean registered = false;

    @Getter
    private EventListener eventListener;

    @Getter
    private String os;

    @EqualsAndHashCode.Include()
    @Getter
    private UUID uuid;

    @Getter
    @Setter
    private VersionData versionData;



    @Getter
    private final Channel channel;

    @Setter
    private String deviceName;

    /**
     * The keypair encryption
     * Used in initial connection establishment
     */
    @Getter
    private KeyPair tempKeyPair;


    @Getter
    private SecretKey secretKey;

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    private Map<Class<? extends Packet>, Pair<Integer, Long>> packetIdMap = new HashMap<>();


    private final StopWatch pingStopWatch = new StopWatch();
    private long lastPacketSentTime = System.currentTimeMillis();

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
        this.tempKeyPair = null;
    }

    private TaskInfo keyTask;

    public void awaitKeys() {
        if(keyTask != null) keyTask.awaitFinish(0);
    }

    public ClientConnection(Server server, Channel channel, UUID uuid) {
        this.server = server;
        this.channel = channel;
        this.uuid = uuid;

        this.keyTask = ThreadUtils.runAsync(new Task() {
            @Override
            public void run(InterfaceTaskInfo<?, Task> taskInfo) {
                tempKeyPair = RSAEncryptionUtil.generateKeyPairs();
                taskInfo.finish(this);
            }
        });

        eventListener = new EventListener(server, this);
    }


    /**
     *
     * @param timeUnit the time to use
     * @return ping delay of client
     */
    @APIUsage
    public long getPingDelay(TimeUnit timeUnit) {
        return pingStopWatch.getTime(timeUnit);
    }

    private Pair<Integer, Long> updatePacketIdPair(Class<? extends Packet> packet, int newId) {
        Pair<Integer, Long> packetIdPair = packetIdMap.get(packet);

        if (packetIdPair == null)
            packetIdPair = new ImmutablePair<>(0, System.currentTimeMillis());
        else {
            if (newId == -1) newId = packetIdPair.getKey() + 1;

            packetIdPair = new ImmutablePair<>(newId, System.currentTimeMillis());
        }

        packetIdMap.put(packet, packetIdPair);
        return packetIdPair;
    }

    /**
     *
     * @param packet Packet to send
     * @param encrypt if true the packet will be encrypted
     */
    @APIUsage
    public ChannelFuture sendObject(@NonNull Packet packet, boolean encrypt) {
        StaticHandler.getCore().getLogger().debug("Sending packet {}:{}", packet.getPacketName(), encrypt);

        Pair<Integer, Long> packetIdPair = updatePacketIdPair(packet.getClass(), -1);

        if (packetIdPair.getLeft() > server.getMaxPacketId() || System.currentTimeMillis() - packetIdPair.getRight() > 900) updatePacketIdPair(packet.getClass(), 0);

        if (encrypt) {
            return channel.writeAndFlush(packet);
        } else {
            return channel.writeAndFlush(new UnencryptedPacketWrapper(packet, packetIdPair.getKey()));
        }
    }



    /**
     * Default usage for {@link #sendObject(Packet, boolean)}
     * @param packet Packet to send
     */
    @APIUsage
    public ChannelFuture sendObject(Packet packet) {
        return sendObject(packet,true);
    }

    /**
     * Closes connection
     */
    public void close() {

        //DISCONNECT FROM SERVER
        Server.getLogger().info("Closing player {}", this);

        if (channel != null) {

            channel.close();


            server.getPlayerHandler().getChannelMap().remove(channel);
        }

        connected = false;
        server.getPlayerHandler().getUuidMap().remove(uuid);


        //serverSocket.close();
    }


    @Override
    public String toString() {
        return "[" + getAddress() + "|" + deviceName +"]";
    }


    /**
     *
     * @return the client address
     */
    public String getAddress() {
        if (channel.remoteAddress() == null) {
            return null;
        }

        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();

        return address.getAddress().toString();
    }

    /**
     * Pings player
     */
    public void ping() {
        pingStopWatch.reset();
        pingStopWatch.start();
        sendObject(new PingPacket(),false);
    }

    @Override
    public void sendPacket(Packet packet) {
        sendObject(packet);
    }

    @Override
    public String getName() {
        return deviceName;
    }

    public void finishPing() {
        pingStopWatch.stop();
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    public Pair<Integer, Long> getPacketId(Class<? extends Packet> packet) {
        packetIdMap.computeIfAbsent(packet, aClass -> new ImmutablePair<>(0,(long) -1));

        return packetIdMap.get(packet);
    }


    void finishConstruct(String name, String os) {
        this.deviceName = name;
        this.os = os;
    }
}
