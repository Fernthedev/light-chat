package com.github.fernthedev.client;

import com.github.fernthedev.client.netty.ClientHandler;
import com.github.fernthedev.exceptions.DebugException;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.universal.StaticHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.ArrayList;
import java.util.List;

public class ClientThread implements Runnable {

    private boolean isRegistered() {
        return client.registered;
    }


    boolean running;

     boolean connected;


    private EventListener listener;
    private Client client;

    boolean connectToServer;

    private Thread readingThread;

    private ChannelFuture future;
    private Channel channel;

    private EventLoopGroup workerGroup;

    /**
     * PingPong delay
     */
    public static long startTime;
    public static long endTime;

    /**
     * is nanosecond
     */
    public static long miliPingDelay;

    //private ReadListener readListener;


    public ClientThread(Client client) {
        this.client = client;
        listener = new EventListener(client);
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    void connect() {
        Client.getLogger().info("Connecting to server.");

        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.TCP_NODELAY,true);
            b.option(ChannelOption.SO_TIMEOUT,5000);
            b.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch)
                        throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            new ClientHandler(client, listener));
                }
            });

            future = b.connect(client.host, client.port).sync();
            channel = future.channel();




            //future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        if (future.isSuccess() && future.channel().isActive()) {
            Client.getLogger().info("SOCKET CONNECTED!");
            connected = true;


            if (!Client.currentThread.isAlive()) {
                running = true;
                Client.currentThread.start();
            }

            if(!waitForCommand.running) {
                client.running = true;
               // client.getLogger().info("NEW WAIT FOR COMMAND THREAD");
                Client.waitThread = new Thread(Client.WaitForCommand);
                Client.waitThread.start();
                Client.getLogger().info("Command thread started");
            }

            //setReadListener();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));

        }
    }



    void sendObject(Object packet) {
        if (packet instanceof Packet) {
            if (channel.isActive()) {
                channel.writeAndFlush(packet);

            }
        }else {
            Client.getLogger().info("not packet");
        }
    }

    public void disconnect() {
        Client.getLogger().info("Disconnecting from server");
        running = false;
        try {

            future.channel().closeFuture().sync();
            workerGroup.shutdownGracefully();

            Client.getLogger().info("Disconnected");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            Client.getLogger().info("Closing connection.");
            running = false;

            //DISCONNECT FROM SERVER
            if(channel.isActive()) {
                if(StaticHandler.isDebug) {
                    try {
                        throw new DebugException();
                    } catch (DebugException e) {
                        e.printStackTrace();
                    }
                }

                if(channel.isActive()) {

                    channel.closeFuture().sync();
                    Client.getLogger().info("Closed connection.");
                }
            }

            Client.getLogger().debug("Closing client!");
            Main.scanner.close();

            new Thread(() -> {
                List<Thread> threads = new ArrayList<>(Thread.getAllStackTraces().keySet());
                threads.remove(Thread.currentThread());
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Client.getLogger().error(e.getMessage(),e.getCause());
                    }
                }
            });

            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        //client.print(running);
       // client.print("Checking for " + client.host + ":" + client.port + " socket " + channel);
        while (running) {
            if (System.console() == null && !StaticHandler.isDebug) close();
        }

    }
}
