package com.github.fernthedev.server;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.VersionData;
import com.github.fernthedev.core.api.APIUsage;
import com.github.fernthedev.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.core.encryption.util.RSAEncryptionUtil;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.latency.PingPacket;
import com.github.fernthedev.fernutils.thread.InterfaceTaskInfo;
import com.github.fernthedev.fernutils.thread.Task;
import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.fernutils.thread.single.TaskInfo;
import io.netty.channel.Channel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Handles client data
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClientPlayer implements SenderInterface, AutoCloseable {

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


    private final StopWatch pingStopWatch = new StopWatch();

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
        this.tempKeyPair = null;
    }

    private TaskInfo keyTask;

    public void awaitKeys() {
        if(keyTask != null) keyTask.awaitFinish(0);
    }

    public ClientPlayer(Server server, Channel channel, UUID uuid) {
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

    /**
     *
     * @param packet Packet to send
     * @param encrypt if true the packet will be encrypted
     */
    public void sendObject(@NonNull Packet packet, boolean encrypt) {
        StaticHandler.getCore().getLogger().debug("Sending packet {}:{}", packet.getPacketName(), encrypt);
        if (encrypt) {
            channel.writeAndFlush(packet);
        } else {
            channel.writeAndFlush(new UnencryptedPacketWrapper(packet));
        }
    }


    /**
     * Default usage for {@link #sendObject(Packet, boolean)}
     * @param packet Packet to send
     */
    public void sendObject(Packet packet) {
        sendObject(packet,true);
    }

    /**
     * Closes connection
     */
    public void close() {

        //DISCONNECT FROM SERVER
        Server.getLogger().info("Closing player {}", this);

        if (channel != null) {

            channel.close();


            PlayerHandler.getChannelMap().remove(channel);
        }

        connected = false;
        PlayerHandler.getUuidMap().remove(uuid);

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

    public static void pingAll() {
        for(ClientPlayer clientPlayer : PlayerHandler.getChannelMap().values()) {
            clientPlayer.ping();
        }
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

    void finishConstruct(String name, String os) {
        this.deviceName = name;
        this.os = os;
    }
}
