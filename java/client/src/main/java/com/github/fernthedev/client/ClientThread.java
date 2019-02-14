package com.github.fernthedev.client;

import com.github.fernthedev.client.netty.ClientHandler;
import com.github.fernthedev.exceptions.DebugException;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.universal.EncryptionHandler;
import com.github.fernthedev.universal.StaticHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;

import javax.crypto.SealedObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

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
        Client.getLogger().info("Connecting to server.");


        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.TCP_NODELAY,true);

            b.handler(new ChannelInitializer<Channel>() {

                @Override
                public void initChannel(Channel ch)
                        throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            clientHandler);
                }
            });

            future = b.connect(client.host, client.port).await();
            channel = future.channel();

        } catch (InterruptedException e) {
            Client.getLogger().log(Level.WARNING,e.getMessage(),e.getCause());
        }



        if (future.isSuccess() && future.channel().isActive()) {
            Client.getLogger().info("CONNECTED!");
            connected = true;


            if (!Client.currentThread.isAlive()) {
                running = true;

                Client.currentThread.start();
            }

            if(!WaitForCommand.running) {
                client.running = true;
               // client.getLogger().info("NEW WAIT FOR COMMAND THREAD");
                Client.waitThread = new Thread(Client.waitForCommand,"CommandThread");
                Client.waitThread.start();
                Client.getLogger().info("Command thread started");
            }

        }
    }



    public void sendObject(Packet packet,boolean encrypt) {
        if (packet != null) {

            if (encrypt) {
                SealedObject sealedObject = EncryptionHandler.encrypt(packet, client.getServerKey());

                channel.writeAndFlush(sealedObject);

            } else {
                channel.writeAndFlush(packet);
            }

        } else {
            Client.getLogger().info("not packet");
        }
    }

    public void sendObject(Packet packet) {
        sendObject(packet,true);
    }

    public void disconnect() {
        Client.getLogger().info("Disconnecting from server");
        running = false;
        try {

            future.channel().closeFuture().sync();
            workerGroup.shutdownGracefully();

            Client.getLogger().info("Disconnected");
        } catch (InterruptedException e) {
            Client.getLogger().log(Level.WARNING,e.getMessage(),e.getCause());
        }
    }

    public void close() {
        try {
            Client.getLogger().info("Closing connection.");
            running = false;

            if(channel != null) {
                //DISCONNECT FROM SERVER
                if (channel.isActive()) {
                    if (StaticHandler.isDebug) {
                        try {
                            throw new DebugException();
                        } catch (DebugException e) {
                            Client.getLogger().log(Level.WARNING, e.getMessage(), e.getCause());
                        }
                    }

                    if (channel.isActive()) {

                        channel.closeFuture().sync();
                        Client.getLogger().info("Closed connection.");
                    }
                }
            }

            Client.getLogger().log(Level.FINE,"Closing client!");

            new Thread(() -> {
                List<Thread> threads = new ArrayList<>(Thread.getAllStackTraces().keySet());
                threads.remove(Thread.currentThread());
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Client.getLogger().log(Level.WARNING,e.getMessage(),e.getCause());
                    }
                }
            },"QuitThread").start();
        } catch (InterruptedException e) {
            Client.getLogger().log(Level.WARNING,e.getMessage(),e.getCause());
        }


    }

    public void run() {
        //client.print(running);
       // client.print("Checking for " + client.host + ":" + client.port + " socket " + channel);
        while (running & client.isCloseConsole()) {
            if (System.console() == null && !StaticHandler.isDebug) close();
        }

    }
}
