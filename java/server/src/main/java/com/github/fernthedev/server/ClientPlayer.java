package com.github.fernthedev.server;

import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.packets.latency.PingPacket;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import static com.github.fernthedev.server.Server.socketList;

public class ClientPlayer implements CommandSender {

    private ServerThread thread;

    private boolean connected;

    public boolean registered = false;

    public String os;

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Channel channel;

    private String deviceName;

    private int id = -1;

    public long delayTime;

    public String getDeviceName() {
        return deviceName;
    }


    public void setThread(ServerThread thread) {
        this.thread = thread;
    }

    public boolean isConnected() {
        return connected;
    }

    public ClientPlayer(Channel channel) {
        this.channel = channel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * PingPong delay
     */
    public long startTime;
    public long endTime;

    void setLastPacket(Object packet) {
        if (packet instanceof Packet) {
        }
    }

    public void sendObject(Object packet) {
        if (packet instanceof Packet) {

            channel.writeAndFlush(packet);
            // out.flush();
           /* if(!(packet instanceof PingPacket)) {
                Server.getLogger().info("Sent " + packet);
            }*/

        } else {
            Server.getLogger().info("not packet");
        }
    }

    public void close() {
        //DISCONNECT FROM SERVER
        Server.getLogger().info("Closing player " + this.toString());

        if (channel != null) {

            channel.close();


            socketList.remove(channel);
            Server.channelServerHashMap.remove(channel);
        }

        connected = false;
        Thread threadThing = thread.shutdown();

        Server.closeThread(threadThing);
        PlayerHandler.players.remove(getId());

        //serverSocket.close();
    }


    @Override
    public String toString() {


        return "[" + getAdress() + "] [" + deviceName + "|" + id +"]";
    }


    public String getAdress() {
        if (channel.remoteAddress() == null) {
            return "unknown";
        }

        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();

        return address.getAddress().toString();
    }

    public void ping() {
        startTime = System.nanoTime();
        sendPacket(new PingPacket());
    }

    public static void pingAll() {
        for(ClientPlayer clientPlayer : socketList.values()) {
            clientPlayer.ping();
        }
    }

    @Override
    public void sendPacket(Packet packet) {
        sendObject(packet);
    }

    @Override
    public void sendMessage(String message) {
        sendPacket(new MessagePacket(message));
    }

    @Override
    public String getName() {
        return deviceName;
    }
}
