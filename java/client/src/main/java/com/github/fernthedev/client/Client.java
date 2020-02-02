package com.github.fernthedev.client;

import com.github.fernthedev.client.backend.AutoCompleteHandler;
import com.github.fernthedev.client.netty.ClientHandler;
import com.github.fernthedev.client.netty.MulticastClient;
import com.github.fernthedev.core.ConsoleHandler;
import com.github.fernthedev.core.MulticastData;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.VersionData;
import com.github.fernthedev.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.core.encryption.codecs.gson.EncryptedGSONObjectDecoder;
import com.github.fernthedev.core.encryption.codecs.gson.EncryptedGSONObjectEncoder;
import com.github.fernthedev.core.exceptions.DebugException;
import com.github.fernthedev.core.packets.CommandPacket;
import com.github.fernthedev.core.packets.MessagePacket;
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
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;


public class Client implements IEncryptionKeyHolder {
    public boolean registered;

    @Getter
    @Setter
    private boolean running = false;

    protected static Logger logger = logger = LoggerFactory.getLogger(Client.class);


    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        java.util.logging.Logger.getLogger("io.netty").setLevel(Level.OFF);
        StaticHandler.setupLoggers();

        String host = null;
        int port = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("-port")) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                    if (port < 0) {
                        logger.error("-port cannot be less than 0");
                        port = -1;
                    } else logger.info("Using port {}", args[i+ + 1]);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    logger.error("-port is not a number");
                    port = -1;
                }
            }

            if (arg.equalsIgnoreCase("-ip") || arg.equalsIgnoreCase("-host")) {
                try {
                    host = args[i + 1];
                    logger.info("Using host {}", args[i+ + 1]);
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Cannot find argument for -host");
                    host = null;
                }
            }

            if (arg.equalsIgnoreCase("-debug")) {
                StaticHandler.setDebug(true);
                logger.debug("Debug enabled");
            }
        }




        if (System.console() == null && !StaticHandler.isDebug()) {

            String filename = Client.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            logger.info("No console found. Starting with CMD assuming it's Windows");

            String[] newArgs = new String[]{"cmd", "/c", "start", "cmd", "/c", "java -jar -Xmx2G -Xms2G \"" + filename + "\""};

            List<String> launchArgs = new ArrayList<>(Arrays.asList(newArgs));
            launchArgs.addAll(Arrays.asList(args));

            try {
                Runtime.getRuntime().exec(launchArgs.toArray(new String[]{}));
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        MulticastClient multicastClient;
        Scanner scanner = new Scanner(System.in);
        if (host == null || host.equals("") || port == -1) {
            multicastClient = new MulticastClient();
            Pair<String, Integer> hostPortPair = check(multicastClient, scanner,4);

            if (hostPortPair != null) {
                host = hostPortPair.getLeft();
                port = hostPortPair.getRight();
            }
        }

        while (host == null || host.equalsIgnoreCase("") || port == -1) {
            if (host == null || host.equals(""))
                host = readLine(scanner, "Host:");

            if (port == -1)
                port = readInt(scanner, "Port:");
        }




        new Client(host, port);
    }

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        StaticHandler.setCore(new ClientCore(this));
        registerLogger();

        initialize(host, port);
    }

    private static String readLine(Scanner scanner, String message) {
        if (!(message == null || message.equals(""))) {
            logger.info(message);
        }
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        } else return null;
    }

    private static int readInt(Scanner scanner, String message) {
        if (!(message == null || message.equals(""))) {
            logger.info(message);
        }
        if (scanner.hasNextLine()) {
            return scanner.nextInt();
        } else return -1;
    }


    private static Pair<String, Integer> check(MulticastClient multicastClient, Scanner scanner, int amount) {
        logger.info("Looking for MultiCast servers");
        multicastClient.checkServers(amount);

        String host = null;
        int port = -1;

        if (!multicastClient.serversAddress.isEmpty()) {
            Map<Integer, MulticastData> servers = new HashMap<>();
            logger.info("Select one of these servers, or use none to skip, refresh to refresh");
            int index = 0;
            for (MulticastData serverAddress : multicastClient.serversAddress) {
                index++;
                servers.put(index, serverAddress);
                
                DefaultArtifactVersion serverCurrent = new DefaultArtifactVersion(serverAddress.getVersion());
                DefaultArtifactVersion serverMin = new DefaultArtifactVersion(serverAddress.getMinVersion());

                StaticHandler.VERSION_RANGE range = StaticHandler.getVersionRangeStatus(new VersionData(serverCurrent, serverMin));
                
                if (range == StaticHandler.VERSION_RANGE.MATCH_REQUIREMENTS){
                    System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort());
                } else {
                    // Current version is smaller than the server's required minimum
                    if(range == StaticHandler.VERSION_RANGE.WE_ARE_LOWER) {
                        System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort() + " (Server's required minimum version is " + serverAddress.getMinVersion() + " while your current version is smaller {" + StaticHandler.getVERSION_DATA().getVersion() + "} Incompatibility issues may arise)");
                    }

                    // Current version is larger than server's minimum version
                    if (range == StaticHandler.VERSION_RANGE.WE_ARE_HIGHER) {
                        System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort() + " (Server's version is " + serverAddress.getVersion() + " while your minimum version is larger {" + StaticHandler.getVERSION_DATA().getMinVersion() + "} Incompatibility issues may arise)");
                    }

                }
            }

            while (scanner.hasNextLine()) {
                String answer = scanner.nextLine();

                answer = answer.replaceAll(" ", "");

                if (answer.matches("[0-9]+")) {
                    try {
                        int serverIndex = Integer.parseInt(answer);

                        if (servers.containsKey(serverIndex)) {
                            MulticastData serverAddress = servers.get(index);

                            host = serverAddress.getAddress();
                            port = serverAddress.getPort();
                            logger.info("Selected {}:{}", serverAddress.getAddress(), serverAddress.getPort());
                            break;
                        } else {
                            logger.info("Not in the list");
                        }
                    } catch (NumberFormatException ignored) {
                        logger.info("Not a number or refresh/none");
                    }
                }

                switch (answer) {
                    case "none":
                        return null;
                    case "refresh":
                        return check(multicastClient, scanner, 7);
                    default:
                        logger.info("Unknown argument");
                        break;
                }
            }
        }

        return new ImmutablePair<>(host, port);
    }



    @Getter
    protected IOSCheck osCheck;

    private int port;
    private String host;

    protected EventListener listener;

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

//    @Getter
//    private Cipher decryptCipher;
//
//    @Getter
//    private Cipher encryptCipher;

    @Getter
    private SecretKey secretKey;

    /**
     * PingPong delay
     */
    public static long startTime;
    public static long endTime;

    /**
     * is nanosecond
     */
    public static long miliPingDelay;

    public void setup() {
        completeHandler = new AutoCompleteHandler(this);

        registerLogger();
        running = true;

        new Thread(() -> {
            getLogger().info("Starting console handler");
            new ConsoleHandler(completeHandler).start();
        },"ConsoleHandler").start();

        getLogger().info("Started console handler");
    }

    public void initialize(String host, int port) {
        setup();
        getLogger().info("Initializing");
        StaticHandler.displayVersion();

        this.port = port;
        this.host = host;

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

        connect();
    }





    protected void registerOSCheck() {
        osCheck = new DesktopOSCheck();
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

    public void connect() {
        getLogger().info("Connecting to server.");

        Bootstrap b = new Bootstrap();
        workerGroup = new NioEventLoopGroup();

        Class<? extends AbstractChannel> channelClass = NioSocketChannel.class;

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

                    ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(StaticHandler.LINE_LIMIT));
                    ch.pipeline().addLast("stringDecoder",new EncryptedGSONObjectDecoder(CharsetUtil.UTF_8, Client.this));

                    ch.pipeline().addLast("stringEncoder", new EncryptedGSONObjectEncoder(CharsetUtil.UTF_8, Client.this));

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
            getLogger().error(e.getMessage(), e.getCause());
        }


        if (future.isSuccess() && future.channel().isActive()) {


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
//            SealedObject sealedObject = encryptObject(packet);
            channel.writeAndFlush(packet);
        } else {
            channel.writeAndFlush(new UnencryptedPacketWrapper(packet));
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
        }

        getLogger().info("Closing client!");
        System.exit(0);
    }


//    @Deprecated
//    public Object decryptObject(SealedObject sealedObject) {
//        if (decryptCipher == null)
//            throw new IllegalArgumentException("Register cipher with registerDecryptCipher() first");
//        try {
//            return sealedObject.getObject(decryptCipher);
//        } catch (IOException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    @Deprecated
//    public Cipher registerDecryptCipher(String key) {
//        try {
//            byte[] salt = new byte[16];
//
//            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
//            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());
//            Cipher cipher = Cipher.getInstance(StaticHandler.getCipherTransformationOld());
//
//            cipher.init(Cipher.DECRYPT_MODE, secret);
//            return cipher;
//        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//    @Deprecated
//    public Cipher registerEncryptCipher(String password) {
//        try {
//            byte[] salt = new byte[16];
//
//            SecretKeyFactory factory = SecretKeyFactory.getInstance(StaticHandler.getKeyFactoryString());
//            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), StaticHandler.getKeySpecTransformation());
//
//            Cipher cipher = Cipher.getInstance(StaticHandler.getCipherTransformationOld());
//            cipher.init(Cipher.ENCRYPT_MODE, secret);
//            return cipher;
//        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


//    @Deprecated
//    public SealedObject encryptObject(Serializable object) {
//        try {
//            if (encryptCipher == null)
//                throw new IllegalArgumentException("Register cipher with registerEncryptCipher() first");
//
//            return new SealedObject(object, encryptCipher);
//        } catch (IOException | IllegalBlockSizeException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

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
//        try {
//            encryptCipher = Cipher.getInstance(StaticHandler.getObjecrCipherTrans());
//            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
//
//            decryptCipher = Cipher.getInstance(StaticHandler.getObjecrCipherTrans());
//            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        }
    }
}
