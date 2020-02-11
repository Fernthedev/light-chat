package com.github.fernthedev.server;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.core.ColorCode;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.encryption.codecs.AcceptablePacketTypes;
import com.github.fernthedev.core.encryption.codecs.fastjson.EncryptedFastJSONObjectDecoder;
import com.github.fernthedev.core.encryption.codecs.fastjson.EncryptedFastJSONObjectEncoder;
import com.github.fernthedev.core.encryption.codecs.gson.EncryptedGSONObjectDecoder;
import com.github.fernthedev.core.encryption.codecs.gson.EncryptedGSONObjectEncoder;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.SelfMessagePacket;
import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.fernutils.thread.single.TaskInfo;
import com.github.fernthedev.server.api.IPacketHandler;
import com.github.fernthedev.server.netty.MulticastServer;
import com.github.fernthedev.server.netty.ProcessingHandler;
import com.github.fernthedev.server.plugin.PluginManager;
import com.github.fernthedev.server.settings.NoFileConfig;
import com.github.fernthedev.server.settings.Settings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Server implements Runnable {
    
    @Getter
    @Setter
    private String name;

    @Getter
    private int port;

    private boolean running;

    private Console console;

    private PluginManager pluginManager;

    @Getter
    @Setter
    private Config<Settings> settingsManager = new NoFileConfig<>(new Settings());

    private MulticastServer multicastServer;


    public Console getConsole() {
        return console;
    }

    @Getter
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private ChannelFuture future;
    private EventLoopGroup bossGroup, workerGroup;

    private ProcessingHandler processingHandler;



    @Getter
    private List<IPacketHandler> packetHandlers = new ArrayList<>();

    public void addPacketHandler(IPacketHandler iPacketHandler) {
        packetHandlers.add(iPacketHandler);
    }

    public void removePacketHandler(IPacketHandler packetHandler) {
        packetHandlers.remove(packetHandler);
    }



    public Server(int port) {
        running = true;
        this.port = port;


        console = new Console(this);
        StaticHandler.setCore(new ServerCore(this));
//        StaticHandler.setupTerminal(autoCompleteHandler);

        pluginManager = new PluginManager();
//        new Thread(serverCommandHandler, "ServerBackgroundThread").start();
    }



//    private void await() {
//        new Thread(() -> {
//            while (running) {
//                try {
//                    future = future.await().sync();
//
//                    future.channel().closeFuture().sync();
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        },"AwaitThread");
//    }

    public synchronized void sendObjectToAllPlayers(@NonNull Packet packet) {
        new Thread(() -> {
            for(ClientPlayer clientPlayer : PlayerHandler.socketList.values()) {

                if (packet != null) {
                    if (clientPlayer.channel.isActive()) {
                        clientPlayer.sendObject(packet);
                    }
                    clientPlayer.setLastPacket(packet);
                } else {
                    logger.info("not packet");
                }
            }
        }).start();

    }


    public synchronized void shutdownServer() {
        getLogger().info(ColorCode.RED + "Shutting down server.");
        running = false;
        if (multicastServer != null) multicastServer.stopMulticast();

        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();
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
        name = "Server-" + Thread.currentThread().getId();
        StaticHandler.displayVersion();
        List<TaskInfo> tasks = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        check();



        new Thread(new PlayerHandler(this),"PlayerHandlerThread").start();

        tasks.add(ThreadUtils.runAsync(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ClientPlayer clientPlayer : PlayerHandler.socketList.values()) {
                if (clientPlayer.channel.isOpen()) {
                    Server.getLogger().info("Gracefully shutting down");
                    sendObjectToAllPlayers(new SelfMessagePacket(SelfMessagePacket.MessageType.LOST_SERVER_CONNECTION));
                    clientPlayer.close();
                }
            }
        }))));






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
                    multicastServer = new MulticastServer("MultiCast Thread", this, StaticHandler.getMULTICAST_ADDRESS());
                    multicastServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));




//        LoggerManager loggerManager = new LoggerManager();
//        pluginManager.registerEvents(loggerManager, new ServerPlugin());

        logger.info("Running on [{}]", StaticHandler.OS);


//        await();
        tasks.add(ThreadUtils.runAsync(this::initServer));

        for(TaskInfo taskInfo : tasks) taskInfo.awaitFinish(0);



        logger.info("Finished initializing. Took {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private void initServer() {
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


        ServerBootstrap bootstrap = new ServerBootstrap();

        EncryptionKeyFinder keyFinder = new EncryptionKeyFinder();

        MessageToMessageEncoder<AcceptablePacketTypes> encoder;
        MessageToMessageDecoder<ByteBuf> decoder;

        switch (settingsManager.getConfigData().getCodecEnum()) {
            case ALIBABA_FASTJSON:
                encoder = new EncryptedFastJSONObjectEncoder(CharsetUtil.UTF_8, keyFinder);
                decoder = new EncryptedFastJSONObjectDecoder(CharsetUtil.UTF_8, keyFinder);
                break;
            case GSON:
            default:
                encoder = new EncryptedGSONObjectEncoder(CharsetUtil.UTF_8, keyFinder);
                decoder = new EncryptedGSONObjectDecoder(CharsetUtil.UTF_8, keyFinder);
                break;
        }


        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel ch) {

//                        ch.pipeline().addLast(new ObjectEncoder(),
//                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
//                                processingHandler);
                        ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(StaticHandler.LINE_LIMIT));
                        ch.pipeline().addLast("stringDecoder", decoder);

                        ch.pipeline().addLast("stringEncoder", encoder);

                        ch.pipeline().addLast(processingHandler);
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(512, 512, 64 * 1024))
                //       .option(EpollChannelOption.MAX_DATAGRAM_PAYLOAD_SIZE, 512)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);
//                .childOption(ChannelOption.SO_TIMEOUT, 5000);

        logger.info("Server socket registered");

        try {
            future = bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            logger.info("Server started successfully at localhost (Connect with {}) using port {}", InetAddress.getLocalHost().getHostAddress(), port);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }

        if (!future.isSuccess()) {
            logger.info("Failed to bind port");
        } else {
            logger.info("Binded port on {}", future.channel().localAddress());
        }
    }

    private void check() {
        if (System.console() == null && !StaticHandler.isDebug()) shutdownServer();
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public void logInfo(String o, Object... os) {
        getLogger().info("[{}] " + o, getName(), os);
    }
}
