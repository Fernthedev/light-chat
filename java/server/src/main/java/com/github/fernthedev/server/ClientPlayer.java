package com.github.fernthedev.server;

import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.packets.latency.PingPacket;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.universal.EncryptionHandler;
import com.github.fernthedev.universal.StaticHandler;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

import static com.github.fernthedev.server.Server.socketList;

public class ClientPlayer implements CommandSender {
    private boolean connected;

    public boolean registered = false;

    @Getter
    private EventListener eventListener;

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
        encryptCipher = registerEncryptCipher(clientKey);
    }



    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Channel channel;

    private String deviceName;

    @Getter
    @Setter
    private int id = -1;

    @Setter
    @Getter
    private long delayTime = -1;

    @Getter
    @Setter
    private Cipher encryptCipher;

    @Getter
    @Setter
    private Cipher decryptCipher;

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isConnected() {
        return connected;
    }

    public ClientPlayer(Server server,Channel channel, UUID uuid) {
        this.channel = channel;
        this.uuid = uuid;

        serverKey = EncryptionHandler.makeSHA256Hash(uuid.toString());
        decryptCipher = registerDecryptCipher(serverKey);

        eventListener = new EventListener(server, this);
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

    public synchronized void sendObject(@NonNull Packet packet, boolean encrypt) {
            /*
            // Length is 16 byte
            SecretKeySpec sks = new SecretKeySpec(serverKey.getBytes(), StaticHandler.getCipherTransformation());

            // Create cipher
            Cipher cipher = Cipher.getInstance(StaticHandler.getCipherTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, sks);*/
        if (encrypt) {
            SealedObject sealedObject = encryptObject(packet);

            channel.writeAndFlush(sealedObject);
        } else {
            channel.writeAndFlush(packet);
        }


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
            decryptCipher = cipher;
            return cipher;
        } catch (Exception e) {
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

    public Object decryptObject(SealedObject sealedObject) {
        try {
            if (decryptCipher == null)
                throw new IllegalArgumentException("Register cipher with registerDecryptCipher() first");
            return sealedObject.getObject(decryptCipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void sendObject(Packet packet) {
        sendObject(packet,true);
    }

    public void close() {

        //DISCONNECT FROM SERVER
        Server.getLogger().info("Closing player {}", this.toString());

        if (channel != null) {

            channel.close();


            socketList.remove(channel);
        }

        connected = false;
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
