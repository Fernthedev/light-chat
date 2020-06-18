package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.lightchat.core.ColorCode;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.api.APIUsage;
import com.github.fernthedev.lightchat.core.api.ThreadLock;
import com.github.fernthedev.lightchat.core.api.event.api.Listener;
import com.github.fernthedev.lightchat.core.api.plugin.PluginManager;
import com.github.fernthedev.lightchat.core.encryption.codecs.JSONHandler;
import com.github.fernthedev.lightchat.core.encryption.codecs.general.gson.EncryptedJSONObjectDecoder;
import com.github.fernthedev.lightchat.core.encryption.codecs.general.gson.EncryptedJSONObjectEncoder;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import com.github.fernthedev.lightchat.server.api.IPacketHandler;
import com.github.fernthedev.lightchat.server.event.ServerShutdownEvent;
import com.github.fernthedev.lightchat.server.event.ServerStartupEvent;
import com.github.fernthedev.lightchat.server.netty.MulticastServer;
import com.github.fernthedev.lightchat.server.netty.ProcessingHandler;
import com.github.fernthedev.lightchat.server.settings.NoFileConfig;
import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class Server implements Runnable {

    @Getter
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private Thread serverThread;

    @Getter
    private final int port;
    private boolean running;
    private boolean isPortBind = false;

    @Getter
    private final Console console;
    private final PluginManager pluginManager = new PluginManager();

    @Getter
    @Setter
    private Config<ServerSettings> settingsManager = new NoFileConfig<>(new ServerSettings());

    private MulticastServer multicastServer;

    private EventLoopGroup bossGroup, workerGroup;
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
    private final ThreadLock startupLock = new ThreadLock();

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
        StaticHandler.setCore(new ServerCore(this));
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
        }), ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService());
    }


    public synchronized void shutdownServer() {
        getLogger().info(ColorCode.RED + "Shutting down server.");
        running = false;
        if (multicastServer != null) multicastServer.stopMulticast();

        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();

        getPluginManager().callEvent(new ServerShutdownEvent());

        shutdownListeners.parallelStream().forEach(Runnable::run);
    }



    public boolean isRunning() {
        return running;
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
            startupLock.lock();

            if (serverThread == null) serverThread = Thread.currentThread();

            if (running) throw new IllegalStateException("Server is already running");

            running = true;
            StaticHandler.displayVersion();
            List<Future<Void>> tasks = new ArrayList<>();

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            check();


            new Thread(playerHandler = new PlayerHandler(this), "PlayerHandlerThread").start();

            tasks.add(ThreadUtils.runAsync(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (ClientConnection clientConnection : playerHandler.getChannelMap().values()) {
                    if (clientConnection.getChannel().isOpen()) {
                        Server.getLogger().info("Gracefully shutting down");
                        sendObjectToAllPlayers(new SelfMessagePacket(SelfMessagePacket.MessageType.LOST_SERVER_CONNECTION));
                        clientConnection.close();
                    }
                }
            })), ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService()));






        /*
        try {
            new MulticastServer("Multicast",this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

            tasks.add(ThreadUtils.runAsync(() -> {
                if (settingsManager.getConfigData().isUseMulticast()) {
                    getLogger().info("Initializing MultiCast Server");
                    try {
                        multicastServer = new MulticastServer("MultiCast Thread", this, StaticHandler.getMulticastAddress());
                        multicastServer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService()));


//        LoggerManager loggerManager = new LoggerManager();
//        pluginManager.registerEvents(loggerManager, new ServerPlugin());

            logger.info("Running on [{}]", StaticHandler.OS);


//        await();
            tasks.add(ThreadUtils.runAsync((Runnable) this::initServer, ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService()));

            for (Future<Void> taskInfo : tasks) {
                while (!taskInfo.isDone()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


            logger.info("Finished initializing. Took {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));

            getPluginManager().callEvent(new ServerStartupEvent(true));

            startupLock.notifyAllThreads();
        }
    }

    private Server initServer() {
        bossGroup = new NioEventLoopGroup();
        processingHandler = new ProcessingHandler(this);
        workerGroup = new NioEventLoopGroup();
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;

        if (settingsManager.getConfigData().isUseNativeTransport()) {
            if (SystemUtils.IS_OS_LINUX) {
                bossGroup = new EpollEventLoopGroup();
                workerGroup = new EpollEventLoopGroup();

                channelClass = EpollServerSocketChannel.class;
                getLogger().info(ColorCode.GOLD + "OS IS LINUX! USING EPOLL TRANSPORT");
            }

            if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD) {
                bossGroup = new KQueueEventLoopGroup();
                workerGroup = new KQueueEventLoopGroup();

                channelClass = KQueueServerSocketChannel.class;
                getLogger().info(ColorCode.GOLD + "OS IS MAC/BSD! USING KQUEUE TRANSPORT");
            }
        }


        bootstrap = new ServerBootstrap();

        EncryptionKeyFinder keyFinder = new EncryptionKeyFinder(this);

        JSONHandler jsonHandler = settingsManager.getConfigData().getCodec();


        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel ch) {
                        ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(StaticHandler.getLineLimit()));
                        ch.pipeline().addLast("stringDecoder", new EncryptedJSONObjectDecoder(settingsManager.getConfigData().getCharset(), keyFinder, jsonHandler));

                        ch.pipeline().addLast("stringEncoder", new EncryptedJSONObjectEncoder(settingsManager.getConfigData().getCharset(), keyFinder, jsonHandler));

                        ch.pipeline().addLast(processingHandler);

                        ch.pipeline().addLast(channelHandlers.toArray(new ChannelHandler[0]));
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
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

    private void check() {
        if (System.console() == null && !StaticHandler.isDebug()) shutdownServer();
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
    @Synchronized
    public Thread getServerThread() {
        return serverThread;
    }

    public String getName() {
        return serverThread.getName();
    }
}
