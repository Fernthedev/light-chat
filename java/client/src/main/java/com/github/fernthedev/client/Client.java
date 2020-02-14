package com.github.fernthedev.client;

import com.github.fernthedev.client.api.IPacketHandler;
import com.github.fernthedev.client.netty.ClientHandler;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.api.APIUsage;
import com.github.fernthedev.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.core.encryption.codecs.general.gson.EncryptedJSONObjectDecoder;
import com.github.fernthedev.core.encryption.codecs.general.gson.EncryptedJSONObjectEncoder;
import com.github.fernthedev.core.exceptions.DebugException;
import com.github.fernthedev.core.packets.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Client implements IEncryptionKeyHolder {

    protected static Logger logger = LoggerFactory.getLogger(Client.class);
    private static CLogger cLogger;

    @Getter
    protected ClientSettings clientSettings;
    protected EventListener listener;

    @Getter
    protected ClientHandler clientHandler;

    protected ChannelFuture future;
    protected Channel channel;
    protected EventLoopGroup workerGroup;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean registered;



    @Setter
    private boolean running = false;

    @Getter
    private List<IPacketHandler> packetHandlers = new ArrayList<>();

    private int port;
    private String host;

    @Getter
    private String name;

    @Getter
    private SecretKey secretKey;

    private StopWatch stopWatch = new StopWatch();

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        StaticHandler.setCore(new ClientCore(this));
        registerLogger();

        initialize(host, port);
    }

//    @Getter
//    private Cipher decryptCipher;
//
//    @Getter
//    private Cipher encryptCipher;

    @APIUsage
    public void addPacketHandler(IPacketHandler iPacketHandler) {
        packetHandlers.add(iPacketHandler);
    }

    @APIUsage
    public void removePacketHandler(IPacketHandler packetHandler) {
        packetHandlers.remove(packetHandler);
    }

    public void setup() {
        registerLogger();
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


        registerOSCheck();
        registerLogger();


//        StaticHandler.setupTerminal(completeHandler);


        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            getLogger().error(e.getMessage(), e.getCause());
            close();
        }


//        waitForCommand = new WaitForCommand(this);


        listener = new EventListener(this);

        clientHandler = new ClientHandler(this, listener);
    }


    protected void registerOSCheck() {
        clientSettings = new ClientSettings();
    }

    protected void registerLogger() {
        cLogger = new CLogger(logger);
    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public ILogManager getLoggerInterface() {
        if (logger == null) registerLogger();
        return cLogger;
    }

    public Logger getLogger() {
        return logger;
    }

    public void connect() throws InterruptedException {
        getLogger().info("Connecting to server.");

        Bootstrap b = new Bootstrap();
        workerGroup = new NioEventLoopGroup();

        Class<? extends AbstractChannel> channelClass = NioSocketChannel.class;

        if (SystemUtils.IS_OS_LINUX && clientSettings.isRunNatives()) {
            workerGroup = new EpollEventLoopGroup();

            channelClass = EpollSocketChannel.class;
        }

        if (SystemUtils.IS_OS_MAC_OSX && clientSettings.isRunNatives()) {
            workerGroup = new KQueueEventLoopGroup();

            channelClass = KQueueSocketChannel.class;
        }


        try {
            b.group(workerGroup);
            b.channel(channelClass);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.TCP_NODELAY, true);

            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) clientSettings.getTimeoutTime());

            b.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel ch) {
                    ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler((int) clientSettings.getTimeoutTime()));

                    ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(StaticHandler.LINE_LIMIT));
                    ch.pipeline().addLast("stringDecoder", new EncryptedJSONObjectDecoder(clientSettings.getCharset(), Client.this, clientSettings.getJsonHandler()));

                    ch.pipeline().addLast("stringEncoder", new EncryptedJSONObjectEncoder(clientSettings.getCharset(), Client.this, clientSettings.getJsonHandler()));

                    ch.pipeline().addLast(clientHandler);
//                    ch.pipeline().addLast(new ObjectEncoder(),
//                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
//                            clientHandler);
                }
            });

            getLogger().info("Establishing connection");
            future = b.connect(host, port).await().sync();
            channel = future.channel();

        } catch (InterruptedException e) {
            getLogger().error(e.getMessage(), e);
            throw e;
        }


        if (future.isSuccess() && future.channel().isActive()) {


            running = true;
            // getLogger().info("NEW WAIT FOR COMMAND THREAD");
//                waitThread = new Thread(waitForCommand, "CommandThread");
//                waitThread.start();


        }
    }


    public void sendObject(@NonNull Packet packet, boolean encrypt) {
        if (encrypt) {
            channel.writeAndFlush(packet);
        } else {
            channel.writeAndFlush(new UnencryptedPacketWrapper(packet));
        }
        StaticHandler.getCore().getLogger().debug("Sending packet {}:{}", packet.getPacketName(), encrypt);

    }

    public void sendObject(Packet packet) {
        sendObject(packet, true);
    }

    public void disconnect() throws InterruptedException {
        getLogger().info("Disconnecting from server");
        running = false;


        future.channel().closeFuture().sync();


        workerGroup.shutdownGracefully();

        getLogger().info("Disconnected");
    }

    public void close() {
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

        getLogger().info("Closing client!");
        System.exit(0);
    }

    @Override
    public SecretKey getSecretKey(ChannelHandlerContext ctx, Channel channel) {
        return secretKey;
    }

    @Override
    public boolean isEncryptionKeyRegistered(ChannelHandlerContext ctx, Channel channel) {
        return secretKey != null;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isRunning() {
        return running;
    }

    void startPingStopwatch() {
        stopWatch.reset();
        stopWatch.start();
    }

    void endPingStopwatch() {
        stopWatch.stop();
    }

    public long getPingTime(TimeUnit timeUnit) {
        return stopWatch.getTime(timeUnit);
    }

}
