package com.github.fernthedev.client.netty;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.EventListener;
import com.github.fernthedev.packets.ConnectedPacket;
import com.github.fernthedev.universal.StaticHandler;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<GeneratedMessageV3> {

    protected EventListener listener;

    public ClientHandler(Client client,EventListener listener) {
        this.listener = listener;
        this.client = client;
        String os = client.getOSName();
        connectedPacket = ConnectedPacket.newBuilder();
        connectedPacket.setName(client.name).setOs(os).setUuid(client.getUuid().toString());
    }

    protected Client client;

    @Getter
    protected ConnectedPacket.Builder connectedPacket;

    /**
     * Method for Encrypt Plain String Data
     * @param plainText
     * @return encryptedText
     */
    public static String encrypt(String encryptionKey,String plainText) {
        String encryptedText = "";
        try {
            Cipher cipher   = Cipher.getInstance(StaticHandler.getCipherTransformation());
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if(cause instanceof ReadTimeoutException) {
            client.getLogger().info("Timed out connection");
            client.getClientThread().close();
        }else{
            cause.printStackTrace();
            super.exceptionCaught(ctx,cause);
        }
    }

    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p>
     * Is called for each message of type {@link Object}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GeneratedMessageV3 msg) throws Exception {
        Object packet;

        if((Object) msg instanceof SealedObject) {

            SealedObject ob = (SealedObject) (Object) msg;

            //packet = (Packet) EncryptionHandler.decrypt(ob, client.getPrivateKey());
            long startTime = System.nanoTime();
            packet = client.getClientThread().decryptObject(ob);

            //  client.getLogger().info("Decrypted object is " + packet + " took " + (System.nanoTime() - startTime) / 1000000 + "ms");

            listener.received(packet);
        }else if (msg != null){
            packet = msg;

            listener.received(packet);
        }





        //ctx.close();
    }
}
