package com.github.fernthedev.server;

import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.packets.TimedOutRegistration;
import com.github.fernthedev.packets.latency.PingPacket;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public class ServerThread implements Runnable {


    private boolean running;

    private boolean isConnected;

    static List<ClientPlayer> socketList = new ArrayList<ClientPlayer>();

    private Object lastPacket;

    public ClientPlayer clientPlayer;

    private EventListener listener;


    private Channel channel;

    private Thread thread;
    //private ReadListener readListener;

    private Server server;

    public ServerThread(Server server, Channel channel, ClientPlayer clientPlayer, EventListener listener) {
        this.server = server;
        this.clientPlayer = clientPlayer;
        this.listener = listener;
        thread = Thread.currentThread();
        running = true;
        isConnected = true;

        this.channel = channel;
        Server.serverInstanceThreads.add(thread);

    }

    void sendObject(Object packet) {
        if (packet instanceof Packet) {
            if (isConnected) {
                channel.writeAndFlush(packet);
                if(!(packet instanceof PingPacket)) {
                  //  Server.getLogger().info("Sent " + packet);

                    lastPacket = packet;
                }
            }
        }else {
            Server.getLogger().info("not packet");
        }
    }


    synchronized Thread shutdown() {
        try {
            running = false;
            //DISCONNECT FROM SERVER
            if (channel != null) {
                if ((!channel.isActive())) {
                    channel.closeFuture().sync();
                }
            }
            socketList.remove(clientPlayer);
            isConnected = false;


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return thread;
    }

    synchronized Thread close(boolean sendObject) {
        try {
            Server.getLogger().info("Closing connection at for player " + clientPlayer);
            running = false;
            //DISCONNECT FROM SERVER
            //RemovePlayerPacket packet = new RemovePlayerPacket();
            if(channel != null) {

                if ((!channel.isActive())) {
                    Server.getLogger().info("Closing sockets.");

                    channel.closeFuture().sync();

                    socketList.remove(clientPlayer);

                    Server.getLogger().info("Closed sockets ");
                }
            }

            isConnected = false;



        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return thread;
    }

    private int secondsPassed;

    public void run() {
        int registerTimeout = 0;
        while (running) {
            secondsPassed++;
            if(!clientPlayer.registered) {
                registerTimeout++;
            }else registerTimeout = 0;


            if(registerTimeout > 20) {
                clientPlayer.sendObject(new TimedOutRegistration(),false);

                clientPlayer.close();
            }

            if(secondsPassed >= 5) {
                clientPlayer.ping();
                secondsPassed = 0;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    ClientPlayer getPlayer(String address) {
        for(ClientPlayer clientPlayerThing : socketList) {
            if(clientPlayerThing.getAdress().equals(address)) return clientPlayerThing;
        }

        return null;
    }

    boolean isRunning() {
        return running;
    }

}
