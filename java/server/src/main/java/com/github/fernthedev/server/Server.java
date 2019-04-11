package com.github.fernthedev.server;

import com.github.fernthedev.light.AuthenticationManager;
import com.github.fernthedev.light.LightManager;
import com.github.fernthedev.light.SettingsManager;
import com.github.fernthedev.packets.LostServerConnectionPacket;
import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.server.backend.AutoCompleteHandler;
import com.github.fernthedev.server.backend.BanManager;
import com.github.fernthedev.server.backend.CommandMessageParser;
import com.github.fernthedev.server.backend.LoggerManager;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.event.chat.ServerPlugin;
import com.github.fernthedev.server.netty.MulticastServer;
import com.github.fernthedev.server.netty.ProcessingHandler;
import com.github.fernthedev.server.plugin.PluginManager;
import com.github.fernthedev.universal.StaticHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.fernthedev.server.CommandWorkerThread.commandList;


public class Server implements Runnable {

    private int port;

    private boolean running = false;

    private static Thread thread;

    private Console console;
    private BanManager banManager;
    private PluginManager pluginManager;

    @Getter
    private SettingsManager settingsManager;

    @Getter(AccessLevel.PACKAGE)
    private AutoCompleteHandler autoCompleteHandler;
    @Getter
    private CommandMessageParser commandMessageParser;

    public Console getConsole() {
        return console;
    }

    public static ConcurrentMap<Channel,ClientPlayer> socketList = new ConcurrentHashMap<>();
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
        StaticHandler.setupTerminal(server.getAutoCompleteHandler());


        pluginManager = new PluginManager();

        ServerCommandHandler serverCommandHandler = new ServerCommandHandler(this);
        commandMessageParser = new CommandMessageParser(this);
        getPluginManager().registerEvents(commandMessageParser, new ServerPlugin());

        new Thread(serverCommandHandler, "ServerBackgroundThread").start();
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
            for(ClientPlayer clientPlayer : socketList.values()) {

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
        running = false;

        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        System.exit(0);
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

        bossGroup = new NioEventLoopGroup();
        processingHandler = new ProcessingHandler(this);
        workerGroup = new NioEventLoopGroup();

        settingsManager = new SettingsManager(server, new File(SettingsManager.getCurrentPath(), "settings.json"));
        settingsManager.setup();

        server.registerCommand(new Command("settings") {
            @Override
            public void onCommand(CommandSender sender, String[] args) {

                if (args.length == 0) {
                    sender.sendMessage("Possible args: set,get,reload,save");
                } else {
                    boolean authenticated = AuthenticationManager.authenticate(sender);
                    if (authenticated) {
                        long timeStart;
                        long timeEnd;
                        long timeElapsed;
                        String arg = args[0];

                        switch (arg.toLowerCase()) {
                            case "set":
                                if (args.length > 2) {
                                    String oldValue = args[1];
                                    String newValue = args[2];

                                    try {
                                        settingsManager.getSettings().setNewValue(oldValue, newValue);
                                        sender.sendMessage("Set " + oldValue + " to " + newValue);
                                    } catch (ClassCastException | IllegalArgumentException e) {
                                        sender.sendMessage("Error:" + e.getMessage());
                                    }
                                } else sender.sendMessage("Usage: settings set {oldvalue} {newvalue}");
                                break;

                            case "get":
                                if (args.length > 1) {
                                    String key = args[1];

                                    try {
                                        Object value = settingsManager.getSettings().getValue(key);
                                        sender.sendMessage("Value of " + key + ": " + value);
                                    } catch (ClassCastException | IllegalArgumentException e) {
                                        sender.sendMessage("Error:" + e.getMessage());
                                    }
                                } else sender.sendMessage("Usage: settings get {serverKey}");
                                break;

                            case "reload":
                                sender.sendMessage("Reloading.");
                                timeStart = System.nanoTime();
                                settingsManager.saveSettings();

                                settingsManager.load();
                                timeEnd = System.nanoTime();
                                timeElapsed = (timeEnd - timeStart) / 1000000;
                                sender.sendMessage("Finished reloading. Took " + timeElapsed + "ms");
                                break;

                            case "save":
                                sender.sendMessage("Saving.");
                                timeStart = System.nanoTime();
                                settingsManager.saveSettings();
                                timeEnd = System.nanoTime();
                                timeElapsed = (timeEnd - timeStart) / 1000000;
                                sender.sendMessage("Finished saving. Took " + timeElapsed + "ms");
                                break;
                            default:
                                sender.sendMessage("No such argument found " + arg + " found");
                                break;
                        }
                    }
                }
            }
        });

        Class channelClass = NioServerSocketChannel.class;

        if (settingsManager.getSettings().isUseNativeTransport()) {
            if (SystemUtils.IS_OS_LINUX) {
                bossGroup = new EpollEventLoopGroup();
                workerGroup = new EpollEventLoopGroup();

                channelClass = EpollServerSocketChannel.class;
            }

            if (SystemUtils.IS_OS_MAC_OSX) {
                bossGroup = new KQueueEventLoopGroup();
                workerGroup = new KQueueEventLoopGroup();

                channelClass = KQueueServerSocketChannel.class;
            }
        }

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel ch) {

                        ch.pipeline().addLast(new ObjectEncoder(),
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                processingHandler);
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
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
            for (ClientPlayer clientPlayer : socketList.values()) {
                if (clientPlayer.channel.isOpen()) {
                    Server.getLogger().info("Gracefully shutting down");
                    Server.sendObjectToAllPlayers(new LostServerConnectionPacket());
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

        if (settingsManager.getSettings().isUseMulticast()) {
            try {
                MulticastServer multicastServer = new MulticastServer("Multicast Thread", this);
                multicastServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        AuthenticationManager authenticationManager = new AuthenticationManager("changepassword", settingsManager);
        server.registerCommand(authenticationManager);
        server.getPluginManager().registerEvents(authenticationManager, new ServerPlugin());

        LoggerManager loggerManager = new LoggerManager();


        pluginManager.registerEvents(loggerManager, new ServerPlugin());

        logger.info("Running on [" + StaticHandler.os + "]");

        if (StaticHandler.os.equalsIgnoreCase("Linux") || StaticHandler.os.contains("Linux") || StaticHandler.isLight) {
            logger.info("Running LightManager (Note this is for raspberry pies only)");
            Thread thread4 = new Thread(new LightManager(this, settingsManager), "LightManagerThread");
            thread4.start();
        } else {
            logger.info("Detected system is not linux. LightManager will not run (manual run with -lightmanager arg)");
        }


        await();

        tick();

    }


    private void tick() {
        if (System.console() == null && !StaticHandler.isDebug) shutdownServer();
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

    public static synchronized Logger getLogger() {
        if(logger == null) registerLogger();
        return logger;
    }

    public static void registerLogger() {
        logger = LogManager.getLogger(Server.class);
    }

    /**
     * This registers the command
     * @param command Command to be registered
     * @return Returns the instance to use it's usage method
     */
    public Command registerCommand(@NotNull Command command) {
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
