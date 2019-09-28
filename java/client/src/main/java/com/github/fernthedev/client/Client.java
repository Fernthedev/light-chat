package com.github.fernthedev.client;

import com.github.fernthedev.client.backend.AutoCompleteHandler;
import com.github.fernthedev.client.netty.ClientHandler;
import com.github.fernthedev.exceptions.DebugException;
import com.github.fernthedev.packets.CommandPacket;
import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.universal.ConsoleHandler;
import com.github.fernthedev.universal.StaticHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;


public class Client {
    public boolean registered;

    @Getter
    @Setter
    private boolean running = false;

    private static Logger logger;

    public Logger getLog4jLogger() {
        return logger;
    }

    @Getter
    protected IOSCheck osCheck;

    public int port;
    public String host;

    @Getter
    @Setter
    @NonNull
    protected String serverKey;

    @Getter
    @Setter
    @NonNull
    protected String privateKey;

    protected EventListener listener;


    @Getter
    protected UUID uuid;

    public String name;
//    public static WaitForCommand waitForCommand;

    private static CLogger cLogger;

    @Getter
    private AutoCompleteHandler completeHandler;

    @Getter
    protected ClientHandler clientHandler;

    protected ChannelFuture future;
    protected Channel channel;

    protected EventLoopGroup workerGroup;

    @Getter
    @Setter
    private Cipher decryptCipher;

    @Getter
    @Setter
    private Cipher encryptCipher;

    /**
     * PingPong delay
     */
    public static long startTime;
    public static long endTime;

    /**
     * is nanosecond
     */
    public static long miliPingDelay;
    private boolean connected;

    protected void getProperties() {
        System.getProperties().list(System.out);
    }

    public void setup() {
        completeHandler = new AutoCompleteHandler(this);

        registerLogger();

        new Thread(() -> {
            getLogger().info("Starting console handler");
            new ConsoleHandler(completeHandler).start();
        },"ConsoleHandler").start();

        getLogger().info("Started console handler");
    }

    public void initialize(String host, int port) {
        getLogger().info("Initializing");

        this.port = port;
        this.host = host;

        registerOSCheck();
        registerLogger();


//        StaticHandler.setupTerminal(completeHandler);


        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            getLogger().logError(e.getMessage(), e.getCause());
            close();
        }


//        waitForCommand = new WaitForCommand(this);

        connected = false;
        running = true;

        listener = new EventListener(this);
        uuid = UUID.randomUUID();
        clientHandler = new ClientHandler(this, listener);

        connect();
    }





    protected void registerOSCheck() {
        osCheck = new DesktopOSCheck();
    }

    protected void registerLogger() {
        logger = LogManager.getLogger(Client.class.getName());
        cLogger = new CLogger(logger);
    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public ILogManager getLogger() {
        if (logger == null) registerLogger();
        return cLogger;
    }

    public void connect() {
        getLogger().info("Connecting to server.");

        Bootstrap b = new Bootstrap();
        workerGroup = new NioEventLoopGroup();

        Class channelClass = NioSocketChannel.class;

        if (SystemUtils.IS_OS_LINUX && osCheck.runNatives()) {
            workerGroup = new EpollEventLoopGroup();

            channelClass = EpollSocketChannel.class;
        }

        if (SystemUtils.IS_OS_MAC_OSX && osCheck.runNatives()) {
            workerGroup = new KQueueEventLoopGroup();

            channelClass = KQueueSocketChannel.class;
        }


        try {
            b.group(workerGroup);
            b.channel(channelClass);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.TCP_NODELAY, true);

            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);

            b.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel ch)
                        throws Exception {
                    ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(15));
                    ch.pipeline().addLast(new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            clientHandler);
                }
            });

            getLogger().info("Establishing connection");
            future = b.connect(host, port).await().sync();
            channel = future.channel();
            getLogger().info("Established channel connection");

        } catch (InterruptedException e) {
            getLogger().logError(e.getMessage(), e.getCause());
        }


        if (future.isSuccess() && future.channel().isActive()) {
            connected = true;


            running = true;
            // getLogger().info("NEW WAIT FOR COMMAND THREAD");
//                waitThread = new Thread(waitForCommand, "CommandThread");
//                waitThread.start();


        }
    }



    public void sendMessage(String message) {
        try {
            message = message.replaceAll(" {2}", " ");
            if (!message.equals("") && !message.equals(" ")) {

                if (message.startsWith("/")) {
                    sendObject(new CommandPacket(message.substring(1)));
                } else
                    sendObject(new MessagePacket(message));
            }
        } catch (IllegalArgumentException e) {
            getLogger().error("Unable to send message. Cause: " + e.getMessage() + " {" + e.getClass().getName() + "}");
        }
    }

    public void sendObject(@NonNull Packet packet, boolean encrypt) {


        if (encrypt) {
            SealedObject sealedObject = encryptObject(packet);

            channel.writeAndFlush(sealedObject);
        } else {
            channel.writeAndFlush(packet);
        }


    }

    public void sendObject(Packet packet) {
        sendObject(packet,true);
    }

    public void disconnect() {
        getLogger().info("Disconnecting from server");
        running = false;

        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        workerGroup.shutdownGracefully();

        getLogger().info("Disconnected");
    }

    public void close() {
        getLogger().info("Closing connection.");
        running = false;

        if(channel != null) {
            //DISCONNECT FROM SERVER
            if (channel.isActive()) {
                if (StaticHandler.isDebug) {
                    try {
                        throw new DebugException();
                    } catch (DebugException e) {
                        getLogger().logError(e.getMessage(),e.getCause());
                    }
                }

                if (channel.isActive()) {

                    channel.closeFuture();
                    getLogger().info("Closed connection.");
                }
            }
        }

        getLogger().log("Closing client!");
        System.exit(0);
    }


    public Object decryptObject(SealedObject sealedObject) {
        if (decryptCipher == null)
            throw new IllegalArgumentException("Register cipher with registerDecryptCipher() first");
        try {
            return sealedObject.getObject(decryptCipher);
        } catch (IOException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cipher registerDecryptCipher(String key) {
        try {
            byte[] salt = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());
            Cipher cipher = Cipher.getInstance(StaticHandler.getObjecrCipherTrans());

            cipher.init(Cipher.DECRYPT_MODE, secret);
            return cipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SealedObject encryptObject(Serializable object) {
        try {
            if (encryptCipher == null)
                throw new IllegalArgumentException("Register cipher with registerEncryptCipher() first");

            return new SealedObject(object, encryptCipher);
        } catch (IOException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cipher registerEncryptCipher(String password) {
        try {
            byte[] salt = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());

            Cipher cipher = Cipher.getInstance(StaticHandler.getObjecrCipherTrans());
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            return cipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }


}
