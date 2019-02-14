package com.github.fernthedev.server;

import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.packets.latency.PingPacket;
import com.github.fernthedev.universal.EncryptionHandler;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.SealedObject;
import java.net.InetSocketAddress;
import java.util.UUID;

import static com.github.fernthedev.server.Server.socketList;

public class ClientPlayer implements CommandSender {

    private ServerThread thread;

    private boolean connected;

    public boolean registered = false;

    public String os;

    @Getter
    private UUID uuid;

    @Getter
    private UUID clientUUID;

    @Getter
    private String clientKey;

    @Getter
    @Setter
    private String serverKey;

    public void setClientUUID(UUID uuid,String privateKey) {
        this.clientUUID = uuid;

        this.clientKey = EncryptionHandler.decrypt(privateKey,serverKey);
    }



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

    public ClientPlayer(Channel channel,UUID uuid) {
        this.channel = channel;
        this.uuid = uuid;

        serverKey = EncryptionHandler.makeSHA256Hash(uuid.toString());

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

    public void sendObject(Packet packet,boolean encrypt) {
        if (packet != null) {

            channel.writeAndFlush(packet);

            /*
            // Length is 16 byte
            SecretKeySpec sks = new SecretKeySpec(serverKey.getBytes(), StaticHandler.getCipherTransformation());

            // Create cipher
            Cipher cipher = Cipher.getInstance(StaticHandler.getCipherTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, sks);*/


            if(encrypt) {
                SealedObject sealedObject = EncryptionHandler.encrypt(packet, clientKey);


                channel.writeAndFlush(sealedObject);
            }else{
                channel.writeAndFlush(packet);
            }

        } else {
            Server.getLogger().info("not packet");
        }
    }


    public void sendObject(Packet packet) {
        sendObject(packet,true);
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
        sendObject(new PingPacket(),false);
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
