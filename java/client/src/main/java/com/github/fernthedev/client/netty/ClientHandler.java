package com.github.fernthedev.client.netty;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.EventListener;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.StaticHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {

    protected EventListener listener;

    protected Client client;

    @Getter
    protected ConnectedPacket connectedPacket;

    public ClientHandler(Client client,EventListener listener) {
        this.listener = listener;
        this.client = client;
        String os = client.getOSName();

        connectedPacket = new ConnectedPacket(client.name,os);
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        Packet packet;

      //  client.getLogger().info("Received " + msg);

        if(msg instanceof SealedObject) {

            SealedObject ob = (SealedObject) msg;

            //packet = (Packet) EncryptionHandler.decrypt(ob, client.getPrivateKey());
            long startTime = System.nanoTime();
            client.getLogger().error("Sealed object received, not expected.");
//            packet = (Packet) client.decryptObject(ob);

          //  client.getLogger().info("Decrypted object is " + packet + " took " + (System.nanoTime() - startTime) / 1000000 + "ms");

//            listener.received(packet);
        }else if (msg instanceof Packet){
            packet = (Packet) msg;

            listener.received(packet);
        }





        //ctx.close();
    }

    /**
     * Method for Encrypt Plain String Data
     * @param plainText
     * @return encryptedText
     */
    public static String encrypt(String encryptionKey,String plainText) {
        String encryptedText = "";
        try {
            Cipher cipher   = Cipher.getInstance(StaticHandler.getCipherTransformationOld());
            byte[] key      = encryptionKey.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF8"));
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(cipherText);

        } catch (Exception E) {
            System.err.println("Encrypt Exception : "+E.getMessage());
        }
        return encryptedText;
    }



    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        client.getLogger().info("Lost connection to server.");
        client.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if(cause instanceof ReadTimeoutException) {
            client.getLogger().info("Timed out connection");
            client.close();
        }else{
            cause.printStackTrace();
        }
    }
}
