package com.github.fernthedev.lightchat.client;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.lightchat.client.api.IPacketHandler;
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent;
import com.github.fernthedev.lightchat.client.netty.ClientHandler;
import com.github.fernthedev.lightchat.core.CoreSettings;
import com.github.fernthedev.lightchat.core.NoFileConfig;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.api.APIUsage;
import com.github.fernthedev.lightchat.core.api.Async;
import com.github.fernthedev.lightchat.core.api.plugin.PluginManager;
import com.github.fernthedev.lightchat.core.codecs.Codecs;
import com.github.fernthedev.lightchat.core.codecs.JSONHandler;
import com.github.fernthedev.lightchat.core.codecs.general.compression.CompressionAlgorithms;
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectDecoder;
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectEncoder;
import com.github.fernthedev.lightchat.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.lightchat.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.lightchat.core.exceptions.DebugException;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket;
import com.google.common.base.Stopwatch;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.*;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


public class Client implements IEncryptionKeyHolder, AutoCloseable {

    @Getter
    @Setter
    protected Logger logger = LoggerFactory.getLogger(Client.class);


    @Getter
    @Setter
    protected Config<? extends ClientSettings> clientSettingsManager = new NoFileConfig<>(new ClientSettings());

    protected EventListener listener;

    @Getter
    protected ClientHandler clientHandler;

    protected ChannelFuture future;

    @Getter
    protected Channel channel;

    protected EventLoopGroup workerGroup;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean registered;

    @Getter
    private final PluginManager pluginManager = new PluginManager();

    @Setter
    @Getter
    private boolean running = false;

    @Getter
    private final List<IPacketHandler> packetHandlers = new ArrayList<>();

    private int port;
    private String host;

    @Getter
    @Setter
    private String name = null;

    /**
     * Is null until a connection is established
     */
    @Getter
    private CompletableFuture<SecretKey> secretKey;


    private final StopWatch stopWatch = new StopWatch();

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    @NonNull
    private final Map<Class<? extends Packet>, Pair<Integer, Long>> packetIdMap = new HashMap<>();

    @Getter
    @Setter
    private int maxPacketId = StaticHandler.DEFAULT_PACKET_ID_MAX;

    public ConnectedPacket buildConnectedPacket() {
        StaticHandler.getCore().getLogger().debug("Using the name: {}", name);
        String javaVersion = System.getProperty("java.version") + " (" + SystemUtils.JAVA_VM_NAME + ")";

        return new ConnectedPacket(getName(), getOSName(), StaticHandler.getVERSION_DATA(), "Java " + javaVersion);
    }

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        StaticHandler.setCore(new ClientCore(this), false);

        listener = new EventListener(this);
        clientHandler = new ClientHandler(this, listener);

        initialize(host, port);
    }

    @Getter
    private Cipher decryptCipher;

    @Getter
    private Cipher encryptCipher;

    @Getter
    private SecureRandom secureRandom;

    @APIUsage
    public void addPacketHandler(IPacketHandler iPacketHandler) {
        packetHandlers.add(iPacketHandler);
    }

    @APIUsage
    public void removePacketHandler(IPacketHandler packetHandler) {
        packetHandlers.remove(packetHandler);
    }

    public void setup() {
        running = true;
    }

    public void initialize(String host, int port) {
        this.port = port;
        this.host = host;
        initialize();
    }

    public void initialize() {
        setup();
        getLogger().info("Initializing");
        StaticHandler.displayVersion();

        try {
            if (name == null) name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            getLogger().error(e.getMessage(), e.getCause());
            disconnect();
        }
    }


    public String getOSName() {
        return System.getProperty("os.name");
    }

    @APIUsage
    @Async
    public ChannelFuture connect() throws InterruptedException {
        registered = false;
        channel = null;
        future = null;
        workerGroup = null;

        getLogger().info("Connecting to server.");

        Bootstrap b = new Bootstrap();
        workerGroup = new NioEventLoopGroup();

        Class<? extends AbstractChannel> channelClass = NioSocketChannel.class;

        ClientSettings clientSettings = clientSettingsManager.getConfigData();

        if (SystemUtils.IS_OS_LINUX && clientSettings.isRunNatives()) {
            workerGroup = new EpollEventLoopGroup();

            channelClass = EpollSocketChannel.class;
        }

        if (SystemUtils.IS_OS_MAC_OSX && clientSettings.isRunNatives()) {
            workerGroup = new KQueueEventLoopGroup();

            channelClass = KQueueSocketChannel.class;
        }

        JSONHandler jsonHandler = Codecs.getJsonHandler(clientSettings.getCodec());

        if (jsonHandler == null)
            throw new IllegalStateException("The codec " + clientSettings.getCodec() + " was not recognized");

        b.group(workerGroup);
        b.channel(channelClass);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);

        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) clientSettings.getTimeoutTime());

        b.handler(new ChannelInitializer<>() {
            @Override
            public void initChannel(@NotNull Channel ch) {

                // inbound -> up to bottom
                // outbound -> bottom to up
                ch.pipeline().addLast(
                        new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8)
                );

                ch.pipeline().addLast("strDecoder", new StringDecoder(clientSettings.getCharset()));
                ch.pipeline().addLast(
                        new EncryptedJSONObjectDecoder(Client.this, jsonHandler),
                        new LengthFieldPrepender(8)
                );

                ch.pipeline().addLast("strEncoder", new StringEncoder());
                ch.pipeline().addLast(new EncryptedJSONObjectEncoder(clientSettings.getCharset(), Client.this, jsonHandler));

                ch.pipeline().addLast(
                        "handler",
                        clientHandler
                );


                int compression = getClientSettingsManager().getConfigData().getCompressionLevel();
                String compressionAlgorithm = getClientSettingsManager().getConfigData().getCompressionAlgorithm();
                kotlin.Pair<Function<CoreSettings, ? extends MessageToByteEncoder<?>>, Function<CoreSettings, ? extends ByteToMessageDecoder>> compressions = CompressionAlgorithms.getCompressions(compressionAlgorithm);

                MessageToByteEncoder<?> encoder = null;
                ByteToMessageDecoder decoder = compressions.component2().apply(getClientSettingsManager().getConfigData());

                if (compression >= 0)
                    encoder = compressions.component1().apply(getClientSettingsManager().getConfigData());

                if (!compressionAlgorithm.equals("NONE")) {

                    if (decoder != null)
                        ch.pipeline().addBefore("strDecoder", "compressDecoder", decoder);

                    if (compression >= 0 && encoder != null) {
                        ch.pipeline().addBefore("strEncoder", "compressEncoder", encoder);
                        getLogger().info("Using {} {} compression", compressionAlgorithm, compression);
                    }
                }
            }
        });

        getLogger().info("Establishing connection");

        Stopwatch connectTime = Stopwatch.createStarted();

        secretKey = CompletableFuture.supplyAsync(EncryptionUtil::generateSecretKey);

        secretKey.thenAccept(secretKey -> {
            try {
                encryptCipher = EncryptionUtil.getEncryptCipher();
                decryptCipher = EncryptionUtil.getDecryptCipher();

                secureRandom = EncryptionUtil.getSecureRandom(secretKey);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        });

        future = b.connect(host, port);

        return future.addListener(newFuture -> {

            channel = future.channel();

            if (future.isSuccess() && future.channel().isActive()) {

                connectTime.stop();
                StaticHandler.getCore().getLogger().debug("Time taken to connect: {}ms", connectTime.elapsed(TimeUnit.MILLISECONDS));
                running = true;
            }
        });


    }


    private Pair<Integer, Long> updatePacketIdPair(Class<? extends Packet> packet, int newId) {
        Pair<Integer, Long> packetIdPair = getPacketId(packet, null, null);

        if (packetIdPair == null)
            packetIdPair = new ImmutablePair<>(0, System.currentTimeMillis());
        else {
            if (newId == -1) newId = packetIdPair.getKey() + 1;
            packetIdPair = new ImmutablePair<>(newId, System.currentTimeMillis());
        }

        packetIdMap.put(packet, packetIdPair);
        return packetIdPair;
    }

    @APIUsage
    public ChannelFuture sendObject(@NonNull Packet packet, boolean encrypt) {
        StaticHandler.getCore().getLogger().debug("Sending packet {}:{}", packet.getPacketName(), encrypt);

        Pair<Integer, Long> packetIdPair = updatePacketIdPair(packet.getClass(), -1);

        if (packetIdPair.getLeft() > maxPacketId || System.currentTimeMillis() - packetIdPair.getRight() > 900)
            updatePacketIdPair(packet.getClass(), 0);

        if (encrypt) {
            return channel.writeAndFlush(packet);
        } else {
            return channel.writeAndFlush(new UnencryptedPacketWrapper(packet, packetIdPair.getKey()));
        }
    }

    @APIUsage
    public ChannelFuture sendObject(Packet packet) {
        return sendObject(packet, true);
    }

    public void disconnect() {
        disconnect(ServerDisconnectEvent.DisconnectStatus.DISCONNECTED);
    }

    public void disconnect(ServerDisconnectEvent.DisconnectStatus disconnectStatus) {
        getLogger().info("Closing connection.");
        running = false;

        if (channel != null && channel.isActive()) {
            //DISCONNECT FROM SERVER
            if (StaticHandler.isDebug()) {
                try {
                    throw new DebugException();
                } catch (DebugException e) {
                    getLogger().error("Debug stacktrace, not an actual error", e);
                }
            }

            if (channel.isActive()) {

                channel.closeFuture();
                getLogger().info("Closed connection.");
            }

        }

        getPluginManager().callEvent(new ServerDisconnectEvent(channel, disconnectStatus));

        workerGroup.shutdownGracefully();

        channel = null;
        future = null;
        workerGroup = null;

    }

    @Override
    public SecretKey getSecretKey(ChannelHandlerContext ctx, Channel channel) {
        return secretKey.getNow(null);
    }

    @Override
    public Cipher getEncryptCipher(ChannelHandlerContext ctx, Channel channel) {
        return encryptCipher;
    }

    @Override
    public Cipher getDecryptCipher(ChannelHandlerContext ctx, Channel channel) {
        return decryptCipher;
    }

    @SneakyThrows
    @Override
    public SecureRandom getSecureRandom(ChannelHandlerContext ctx, Channel channel) {
        return secureRandom;
    }

    @Override
    public boolean isEncryptionKeyRegistered(ChannelHandlerContext ctx, Channel channel) {
        return secretKey != null;
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    @Override
    public Pair<Integer, Long> getPacketId(@NonNull Class<? extends Packet> clazz, ChannelHandlerContext ctx, Channel channel) {
        packetIdMap.computeIfAbsent(clazz, aClass -> new ImmutablePair<>(0, (long) -1));

        return packetIdMap.get(clazz);
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    @APIUsage
    public Pair<Integer, Long> getPacketId(Class<? extends Packet> clazz) {
        return packetIdMap.get(clazz);
    }


    void startPingStopwatch() {
        stopWatch.reset();
        stopWatch.start();
    }

    void endPingStopwatch() {
        if (!stopWatch.isStopped())
            stopWatch.suspend();
    }

    public long getPingTime(TimeUnit timeUnit) {
        // Return -1 if ping has not been measured yet
        if (!stopWatch.isStarted() && !stopWatch.isSuspended())
            return -1;

        return stopWatch.getTime(timeUnit);
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     */
    @Override
    public void close() {
        disconnect();
    }
}
