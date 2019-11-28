package com.github.fernthedev.server;

import com.github.fernthedev.core.ColorCode;
import com.github.fernthedev.core.ConsoleHandler;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.encryption.codecs.fastjson.EncryptedFastJSONObjectDecoder;
import com.github.fernthedev.core.encryption.codecs.fastjson.EncryptedFastJSONObjectEncoder;
import com.github.fernthedev.core.encryption.codecs.gson.EncryptedGSONObjectDecoder;
import com.github.fernthedev.core.encryption.codecs.gson.EncryptedGSONObjectEncoder;
import com.github.fernthedev.core.packets.MessagePacket;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.SelfMessagePacket;
import com.github.fernthedev.gson.GsonConfig;
import com.github.fernthedev.light.LightManager;
import com.github.fernthedev.light.exceptions.NoPi4JLibsFoundException;
import com.github.fernthedev.server.backend.*;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.command.LightCommand;
import com.github.fernthedev.server.command.SettingsCommand;
import com.github.fernthedev.server.event.chat.ServerPlugin;
import com.github.fernthedev.server.netty.MulticastServer;
import com.github.fernthedev.server.netty.ProcessingHandler;
import com.github.fernthedev.server.plugin.PluginManager;
import com.github.fernthedev.server.settings.Settings;
import io.netty.bootstrap.ServerBootstrap;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.github.fernthedev.server.CommandWorkerThread.commandList;


public class Server implements Runnable {

    private final ServerCommandHandler commandHandler;
    private int port;

    private boolean running = false;

    private static Thread thread;

    private Console console;
    private BanManager banManager;
    private PluginManager pluginManager;

    @Getter
    private static GsonConfig<Settings> settingsManager;

    @Getter(AccessLevel.PACKAGE)
    private AutoCompleteHandler autoCompleteHandler;
    @Getter
    private CommandMessageParser commandMessageParser;
    private MulticastServer multicastServer;


    public Console getConsole() {
        return console;
    }

    static List<Thread> serverInstanceThreads = new ArrayList<>();

    private static Logger logger;

    private ChannelFuture future;
    private EventLoopGroup bossGroup,workerGroup;

    private static Server server;
    private ProcessingHandler processingHandler;


    Server(int port) {
        getLogger();
        running = true;
        this.port = port;
        console = new Console();
        server = this;
        autoCompleteHandler = new AutoCompleteHandler(this);
//        StaticHandler.setupTerminal(autoCompleteHandler);


        pluginManager = new PluginManager();

        commandHandler = new ServerCommandHandler(this);
        commandMessageParser = new CommandMessageParser(this);
        getPluginManager().registerEvents(commandMessageParser, new ServerPlugin());

//        new Thread(serverCommandHandler, "ServerBackgroundThread").start();

    }

    public static Server getInstance() {
        return server;
    }



    private void await() {
        new Thread(() -> {
            while (running) {
                try {
                    future = future.await().sync();

                    future.channel().closeFuture().sync();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"AwaitThread");
    }

    private static synchronized void sendObjectToAllPlayers(@NonNull Packet packet) {
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


    synchronized void shutdownServer() {
        getLogger().info(ColorCode.RED + "Shutting down server.");
        running = false;
        multicastServer.stopMulticast();


        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        System.exit(0);
    }

    public static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
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
        thread = Thread.currentThread();

        new Thread(() -> {
            getLogger().info("Type Command: (try help)");
            new ConsoleHandler(autoCompleteHandler).start();
        }, "ConsoleHandler").start();

        bossGroup = new NioEventLoopGroup();
        processingHandler = new ProcessingHandler(this);
        workerGroup = new NioEventLoopGroup();

        settingsManager = new GsonConfig<>(new Settings(), new File(getCurrentPath(), "settings.json"));
        settingsManager.save();

        server.registerCommand(new SettingsCommand());

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

        MessageToMessageEncoder encoder;
        MessageToMessageDecoder decoder;

        switch (settingsManager.getConfigData().getCodecEnum()) {
            case GSON:
                encoder = new EncryptedGSONObjectEncoder(CharsetUtil.UTF_8, keyFinder);
                decoder = new EncryptedGSONObjectDecoder(CharsetUtil.UTF_8, keyFinder);
                break;
            case ALIBABA_FASTJSON:
                encoder = new EncryptedFastJSONObjectEncoder(CharsetUtil.UTF_8, keyFinder);
                decoder = new EncryptedFastJSONObjectDecoder(CharsetUtil.UTF_8, keyFinder);
                break;
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
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_TIMEOUT, 5000);

        logger.info("Server socket registered");

        new Thread(new PlayerHandler(this),"PlayerHandlerThread").start();
        //Timer pingPongTimer = new Timer("pingpong");


        //await();
        try {
            future = bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ClientPlayer clientPlayer : PlayerHandler.socketList.values()) {
                if (clientPlayer.channel.isOpen()) {
                    Server.getLogger().info("Gracefully shutting down");
                    Server.sendObjectToAllPlayers(new SelfMessagePacket(SelfMessagePacket.MessageType.LOST_SERVER_CONNECTION));
                    clientPlayer.close();
                }
            }
        }));

        try {
            logger.info("Server started successfully at localhost (Connect with " + InetAddress.getLocalHost().getHostAddress() + ") using port " + port);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }

        if (!future.isSuccess()) {
            logger.info("Failed to bind port");
        } else {
            logger.info("Binded port on " + future.channel().localAddress());
        }

        /*
        try {
            new MulticastServer("Multicast",this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        if (settingsManager.getConfigData().isUseMulticast()) {
            try {
                multicastServer = new MulticastServer("Multicast Thread", this);
                multicastServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        AuthenticationManager authenticationManager = new AuthenticationManager("changepassword");
        server.registerCommand(authenticationManager);
        server.getPluginManager().registerEvents(authenticationManager, new ServerPlugin());

//        LoggerManager loggerManager = new LoggerManager();
//        pluginManager.registerEvents(loggerManager, new ServerPlugin());

        logger.info("Running on [{}]", StaticHandler.os);

        if (StaticHandler.os.equalsIgnoreCase("Linux") || StaticHandler.os.contains("Linux") || StaticHandler.isLight) {
            logger.info("Running LightManager (Note this is for raspberry pies only)");

            Thread lightThread = new Thread(() -> {

                try {
                    LightManager.init();
                    registerCommand(new LightCommand());
                    logger.info("Registered");
                } catch (IllegalArgumentException | ExceptionInInitializerError | NoPi4JLibsFoundException e) {
                    logger.error("Unable to load Pi4J Libraries. To load stacktrace, add -debug flag. Message: {}", e.getMessage());
                    if (StaticHandler.isDebug()) {
                        e.printStackTrace();
                        registerCommand(new LightCommand());
                        logger.info("Registered");
                    }
                }

            }, "LightThread");
            registerCommand(new LightCommand());
            lightThread.start();


        } else {
            logger.info("Detected system is not linux. LightManager will not run (manual run with -lightmanager arg)");
        }


        await();

        tick();

    }

    public void dispatchCommand(@NonNull String command) {
        commandHandler.dispatchCommand(command);
    }

    public void dispatchCommand(@NonNull CommandSender sender, @NonNull String command) {
        commandHandler.dispatchCommand(sender, command);
    }

    private void tick() {
        if (System.console() == null && !StaticHandler.isDebug()) shutdownServer();
    }

    public static synchronized void closeThread(Thread thread) {
        if(thread == Server.thread) throw new IllegalArgumentException("Cannot be same thread it's joining on");

        new Thread(() -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e.getCause());
            }
        },"CloseThread");
    }

    public static void sendMessage(String message) {
        Server.getLogger().info(message);
        Server.sendObjectToAllPlayers(new MessagePacket(message));
    }

    public int getPort() {
        return port;
    }

    public static Logger getLogger() {
        if(logger == null) registerLogger();
        return logger;
    }

    public static void registerLogger() {
        logger = LoggerFactory.getLogger(Server.class);
    }

    /**
     * This registers the command
     * @param command Command to be registered
     * @return Returns the instance to use it's usage method
     */
    public Command registerCommand(@NonNull Command command) {
        commandList.add(command);
        return command;
    }

    public synchronized BanManager getBanManager() {
        if(banManager == null) banManager = new BanManager();

        return banManager;
    }

    public List<Command> getCommands() {
        return commandList;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }
}
