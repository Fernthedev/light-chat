package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.fernutils.thread.single.TaskInfo;
import com.github.fernthedev.lightchat.core.ColorCode;
import com.github.fernthedev.lightchat.core.NoFileConfig;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.api.APIUsage;
import com.github.fernthedev.lightchat.core.api.event.api.Listener;
import com.github.fernthedev.lightchat.core.api.plugin.PluginManager;
import com.github.fernthedev.lightchat.core.codecs.JSONHandler;
import com.github.fernthedev.lightchat.core.codecs.general.compression.CompressionAlgorithm;
import com.github.fernthedev.lightchat.core.codecs.general.compression.Compressors;
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectDecoder;
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectEncoder;
import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import com.github.fernthedev.lightchat.server.api.IPacketHandler;
import com.github.fernthedev.lightchat.server.event.ServerShutdownEvent;
import com.github.fernthedev.lightchat.server.event.ServerStartupEvent;
import com.github.fernthedev.lightchat.server.netty.KeyThread;
import com.github.fernthedev.lightchat.server.netty.MulticastServer;
import com.github.fernthedev.lightchat.server.netty.ProcessingHandler;
import com.github.fernthedev.lightchat.server.security.AuthenticationManager;
import com.github.fernthedev.lightchat.server.security.BanManager;
import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;


public class Server implements Runnable {

    @Getter
    @Setter
    private Logger logger = LoggerFactory.getLogger(Server.class);

    private Thread serverThread;

    @Getter
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    @Setter
    @Getter
    private AuthenticationManager authenticationManager = new AuthenticationManager(this);

    @Getter
    @Setter
    private BanManager banManager = new BanManager(this);

    @Getter
    private final int port;
    private volatile boolean running;
    private volatile boolean shutdown = false;
    private boolean isPortBind = false;

    @Getter
    private final Console console;
    private final PluginManager pluginManager = new PluginManager();

    @Getter
    @Setter
    private Config<? extends ServerSettings> settingsManager = new NoFileConfig<>(new ServerSettings());

    private MulticastServer multicastServer;

    private EventLoopGroup bossGroup, workerGroup;


    @Getter
    private final KeyThread<KeyPair> rsaKeyThread;

    private ProcessingHandler processingHandler;

    private final List<ChannelHandler> channelHandlers = new ArrayList<>();

    @Getter
    @Setter
    private int maxPacketId = StaticHandler.DEFAULT_PACKET_ID_MAX;

    /**
     * @deprecated Might be replaced with {@link ServerShutdownEvent}
     * Use event system {@link PluginManager#registerEvents(Listener)}
     *
     */
    @Deprecated
    private List<Runnable> shutdownListeners = new ArrayList<>();

    @Getter
    private final CompletableFuture<Void> startupLock = new CompletableFuture<>();

    @Getter
    private PlayerHandler playerHandler;
    private ServerBootstrap bootstrap;

    /**
     * The listener will be called when {@link #shutdownServer()} is called
     * @param runnable the listener
     */
    @APIUsage
    public void addShutdownListener(Runnable runnable) {
        shutdownListeners.add(runnable);
    }

    /**
     * Add channel handlers for netty functionality
     */
    @APIUsage
    public void addChannelHandler(ChannelHandler channelHandler) {
        channelHandlers.add(channelHandler);
    }


    /**
     * Custom packet handlers added by outside the main server code
     */
    @Getter
    private final List<IPacketHandler> packetHandlers = new ArrayList<>();

    public Server(int port) {
        this.port = port;


        console = new Console(this);
        StaticHandler.setCore(new ServerCore(this), false);

        int rsaKeyPoolSize = 15;

        Supplier<Boolean> serverCondition = () -> running;

        rsaKeyThread = new KeyThread<>(() -> RSAEncryptionUtil.generateKeyPairs(settingsManager.getConfigData().getRsaKeySize()), rsaKeyPoolSize, serverCondition);
    }

    @APIUsage
    public void addPacketHandler(IPacketHandler iPacketHandler) {
        packetHandlers.add(iPacketHandler);
    }

    @APIUsage
    public void removePacketHandler(IPacketHandler packetHandler) {
        packetHandlers.remove(packetHandler);
    }

    public synchronized void sendObjectToAllPlayers(@NonNull Packet packet) {
        ThreadUtils.runAsync((() -> {
            for (ClientConnection clientConnection : playerHandler.getChannelMap().values()) {
                logger.debug("Sending to all {} to {}", packet.getPacketName(), clientConnection);
                clientConnection.sendObject(packet);
            }
        }), executorService);
    }


    @Synchronized
    public void shutdownServer() {
        if (!shutdown) {
            shutdown = true;
            getLogger().info(ColorCode.RED + "Shutting down server.");
            running = false;

            ThreadUtils.runAsync(() -> {
                if (multicastServer != null) multicastServer.stopMulticast();
            }, executorService);



            if (workerGroup != null) workerGroup.shutdownGracefully();
            if (bossGroup != null) bossGroup.shutdownGracefully();

            getPluginManager().callEvent(new ServerShutdownEvent());

            executorService.shutdown();

            shutdownListeners.parallelStream().forEach(Runnable::run);
        } else throw new IllegalStateException("Server is already shutting down!");
    }



    public boolean isRunning() {
        return running && serverThread.isAlive();
    }

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
        serverThread = Thread.currentThread();

        start();
    }

    /**
     * Starts the server in the current thread
     */
    @APIUsage
    public void start() {
        synchronized (startupLock) {

            if (serverThread == null) serverThread = Thread.currentThread();

            if (running) throw new IllegalStateException("Server is already running");

            running = true;
            shutdown = false;

            StaticHandler.displayVersion();
            List<TaskInfo<Void>> tasks = new ArrayList<>();

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();


            new Thread(playerHandler = new PlayerHandler(this), "PlayerHandlerThread").start();

            tasks.add(ThreadUtils.runAsync(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (ClientConnection clientConnection : playerHandler.getChannelMap().values()) {
                    if (clientConnection.getChannel().isOpen()) {
                        getLogger().info("Gracefully shutting down");
                        sendObjectToAllPlayers(new SelfMessagePacket(SelfMessagePacket.MessageType.LOST_SERVER_CONNECTION));
                        clientConnection.close();
                    }
                }
            })), executorService));






        /*
        try {
            new MulticastServer("Multicast",this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

            if (settingsManager.getConfigData().isUseMulticast()) {
                tasks.add(ThreadUtils.runAsync(() -> {
                    getLogger().info("Initializing MultiCast Server");
                    try {
                        multicastServer = new MulticastServer("MultiCast Thread", this, StaticHandler.getMulticastAddress());
                        multicastServer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }, executorService));
            }

            tasks.add(ThreadUtils.runAsync(() -> {
                authenticationManager = new AuthenticationManager(this);
                getPluginManager().registerEvents(authenticationManager);
            }, getExecutorService()));


//        LoggerManager loggerManager = new LoggerManager();
//        pluginManager.registerEvents(loggerManager, new ServerPlugin());

            logger.info("Running on [{}]", StaticHandler.OS);


//        await();
            tasks.add(ThreadUtils.runAsync((Runnable) this::initServer, executorService));

            for (TaskInfo<Void> taskInfo : tasks) {
                taskInfo.awaitFinish(1);
            }


            logger.info("Finished initializing. Took {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));

            ThreadUtils.runAsync(() -> getPluginManager().callEvent(new ServerStartupEvent(true)), executorService);


            startupLock.complete(null);
        }

        while (running) {
            if (bossGroup.isShutdown() || workerGroup.isShutdown()) {
                running = false;
                continue;
            }

            try {
                queue.take().run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            shutdownServer();
        } catch (IllegalStateException ignored) {}
    }

    private Server initServer() {
        bossGroup = new NioEventLoopGroup();
        processingHandler = new ProcessingHandler(this);
        workerGroup = new NioEventLoopGroup();
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;

        if (settingsManager.getConfigData().isUseNativeTransport()) {
            getLogger().debug("Attempting to use native transport if available.");

            if (Epoll.isAvailable() /*|| SystemUtils.IS_OS_LINUX*/) {
                bossGroup = new EpollEventLoopGroup();
                workerGroup = new EpollEventLoopGroup();

                channelClass = EpollServerSocketChannel.class;
                getLogger().info(ColorCode.GOLD + "OS IS LINUX! USING EPOLL TRANSPORT");
            }


            if (KQueue.isAvailable() /*|| SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD*/) {
                bossGroup = new KQueueEventLoopGroup();
                workerGroup = new KQueueEventLoopGroup();

                channelClass = KQueueServerSocketChannel.class;
                getLogger().info(ColorCode.GOLD + "OS IS MAC/BSD! USING KQUEUE TRANSPORT");
            }
        }


        bootstrap = new ServerBootstrap();

        EncryptionKeyFinder keyFinder = new EncryptionKeyFinder(this);

        JSONHandler jsonHandler = settingsManager.getConfigData().getCodec();

        // Start key thread before initializing netty
        rsaKeyThread.setDaemon(true);
        rsaKeyThread.start();
        getLogger().info("Started RSA Key thread pool. Currently {} keys", rsaKeyThread.getKeysInPool());

        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(@NotNull Channel ch) {
                        ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(StaticHandler.getLineLimit()));
                        ch.pipeline().addLast("stringDecoder", new EncryptedJSONObjectDecoder(settingsManager.getConfigData().getCharset(), keyFinder, jsonHandler));


                        ch.pipeline().addLast("stringEncoder", new EncryptedJSONObjectEncoder(settingsManager.getConfigData().getCharset(), keyFinder, jsonHandler));

                        int compression = getSettingsManager().getConfigData().getCompressionLevel();
                        CompressionAlgorithm compressionAlgorithm = getSettingsManager().getConfigData().getCompressionAlgorithm();

                        if (compressionAlgorithm != CompressionAlgorithm.NONE) {
                            ch.pipeline().addFirst(Compressors.getCompressDecoder(compressionAlgorithm, compression));

                            if (compression > 0) {

                                MessageToByteEncoder<ByteBuf> compressEncoder = Compressors.getCompressEncoder(compressionAlgorithm, compression);
                                ch.pipeline().addBefore("stringEncoder", "compressEncoder", compressEncoder);
                                getLogger().info("Using {} {} compression", compressionAlgorithm.name(), compression);
                            }
                        }


                        ch.pipeline().addLast(processingHandler);

                        ch.pipeline().addLast(channelHandlers.toArray(new ChannelHandler[0]));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(512, 512, 64 * 1024))
                //       .option(EpollChannelOption.MAX_DATAGRAM_PAYLOAD_SIZE, 512)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);
//                .childOption(ChannelOption.SO_TIMEOUT, 5000);

        logger.info("Server socket registered");


        return this;
    }

    public ChannelFuture bind() {
        if (isPortBind) throw new IllegalStateException("Port is already set to bind on port " + port);

        return bootstrap.bind(port)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        try {
                            logger.info("Server started successfully at localhost (Connect with {}) using port {}", InetAddress.getLocalHost().getHostAddress(), port);
                        } catch (UnknownHostException e) {
                            logger.error(e.getMessage(), e);
                        }

                        logger.info("Bind port on {}", future.channel().localAddress());
                        isPortBind = true;
                    } else {
                        logger.info("Failed to bind port");
                        isPortBind = false;
                    }
                });
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public void logInfo(String o, Object... os) {
        List<Object> objects = new ArrayList<>();

        objects.add(getName());
        objects.addAll(Arrays.asList(os));

        getLogger().info("[{}] " + o, objects.toArray());
    }

    @APIUsage
    public Thread getServerThread() {
        return serverThread;
    }

    @APIUsage
    public void runOnServerThread(Runnable runnable) {
        if (Thread.currentThread() == serverThread) {
            runnable.run();
        } else {
            queue.add(runnable);
        }
    }

    public String getName() {
        return serverThread.getName();
    }
}
