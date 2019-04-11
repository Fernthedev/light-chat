package com.github.fernthedev.client;

import com.github.fernthedev.client.netty.ClientHandler;
import com.github.fernthedev.exceptions.DebugException;
import com.github.fernthedev.packets.Packet;
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
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

public class ClientThread implements Runnable {

    protected boolean isRegistered() {
        return client.registered;
    }


    public boolean running;

    public boolean connected;


    protected EventListener listener;
    protected Client client;

    public boolean connectToServer;


    protected Thread readingThread;

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

    /**
     * PingPong delay
     */
    public static long startTime;
    public static long endTime;

    /**
     * is nanosecond
     */
    public static long miliPingDelay;

    //protected ReadListener readListener;


    public ClientThread(Client client) {
        this.client = client;
        listener = new EventListener(client);
        running = true;
        client.uuid = UUID.randomUUID();
        clientHandler = new ClientHandler(client, listener);
    }

    public boolean isRunning() {
        return running;
    }

    public void connect() {
        IOSCheck osCheck = client.getOsCheck();
        client.getLogger().info("Connecting to server.");

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

            future = b.connect(client.host, client.port).await().sync();
            channel = future.channel();

        } catch (InterruptedException e) {
            client.getLogger().logError(e.getMessage(), e.getCause());
        }


        if (future.isSuccess() && future.channel().isActive()) {
            connected = true;


            if (!Client.currentThread.isAlive() && runThread) {
                running = true;

                Client.currentThread.start();
            }

            if (!WaitForCommand.running && runThread) {
                client.running = true;
                // client.getLogger().info("NEW WAIT FOR COMMAND THREAD");
                Client.waitThread = new Thread(Client.waitForCommand, "CommandThread");
                Client.waitThread.start();
                client.getLogger().info("Command thread started");
            }

        }
    }

    protected boolean runThread = true;

    public void sendObject(Packet packet,boolean encrypt) {
        if (packet != null) {

            if (encrypt) {
                SealedObject sealedObject = encryptObject(packet);

                channel.writeAndFlush(sealedObject);
            } else {
                channel.writeAndFlush(packet);
            }



        } else {
            client.getLogger().info("not packet");
        }
    }

    public void sendObject(Packet packet) {
        sendObject(packet,true);
    }

    public void disconnect() {
        client.getLogger().info("Disconnecting from server");
        running = false;

        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        workerGroup.shutdownGracefully();

        client.getLogger().info("Disconnected");
    }

    public void close() {
        client.getLogger().info("Closing connection.");
        running = false;

        if(channel != null) {
            //DISCONNECT FROM SERVER
            if (channel.isActive()) {
                if (StaticHandler.isDebug) {
                    try {
                        throw new DebugException();
                    } catch (DebugException e) {
                        client.getLogger().logError(e.getMessage(),e.getCause());
                    }
                }

                if (channel.isActive()) {

                    channel.closeFuture();
                    client.getLogger().info("Closed connection.");
                }
            }
        }

        client.getLogger().log("Closing client!");


    }

    public void run() {
        //client.print(running);
       // client.print("Checking for " + client.host + ":" + client.port + " socket " + channel);
            if (System.console() == null && !StaticHandler.isDebug) close();


    }
}
