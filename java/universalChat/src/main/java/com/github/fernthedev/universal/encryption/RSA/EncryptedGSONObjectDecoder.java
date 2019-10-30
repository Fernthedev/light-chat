package com.github.fernthedev.universal.encryption.RSA;

import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.universal.encryption.EncryptedBytes;
import com.github.fernthedev.universal.encryption.EncryptedPacketWrapper;
import com.github.fernthedev.universal.encryption.PacketWrapper;
import com.github.fernthedev.universal.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.universal.encryption.util.EncryptionUtil;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts encrypted json to a decrypted object
 */
public class EncryptedGSONObjectDecoder extends StringDecoder {

    private static final Gson gson = new Gson();

    protected IEncryptionKeyHolder encryptionKeyHolder;
    protected Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public EncryptedGSONObjectDecoder(IEncryptionKeyHolder encryptionKeyHolder) {
        this(Charset.defaultCharset(), encryptionKeyHolder);
    }

    /**
     * Creates a new instance with the specified character set.
     *
     * @param charset
     */
    public EncryptedGSONObjectDecoder(Charset charset, IEncryptionKeyHolder encryptionKeyHolder) {
        super(charset);
        this.charset = charset;
        this.encryptionKeyHolder = encryptionKeyHolder;
    }

    /**
     * Returns a string list
     *
     * @param ctx
     * @param msg The data received
     * @param out The returned objects
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        List<Object> tempDecodeList = new ArrayList<>();
        super.decode(ctx, msg, tempDecodeList);

        String decodedStr = (String) tempDecodeList.get(0);
        PacketWrapper packetWrapper = gson.fromJson(decodedStr, PacketWrapper.class);

        String decryptedJSON;

        if (packetWrapper.encrypt()) {
            packetWrapper = gson.fromJson(decodedStr, EncryptedPacketWrapper.class);
            decryptedJSON = decrypt(ctx, ((EncryptedPacketWrapper) packetWrapper).getJsonObject());
        } else {
            packetWrapper = gson.fromJson(decodedStr, UnencryptedPacketWrapper.class);
            decryptedJSON = ((UnencryptedPacketWrapper) packetWrapper).getJsonObject();
        }

        try {
            out.add(getParsedObject(packetWrapper.getAClass(), decryptedJSON));
        } catch (ClassNotFoundException e) {
            System.err.println("The class " + packetWrapper.getAClass() + " does not exist in classpath. The sender might be sending malicious code or a different protocol or version of this network software.");
        }


    }

    /**
     * Converts the JSON Object into it's former instance by providing the class name
     * @param aClass
     * @param jsonObject
     * @return
     * @throws ClassNotFoundException
     */
    public Object getParsedObject(String aClass, String jsonObject) throws ClassNotFoundException {
        if(Class.forName(aClass).isInstance(Packet.class)) {
            throw new IllegalArgumentException("The class provided is not a packet type. Received: " + aClass);
        }

        Class<?> tClass = Class.forName(aClass);
        return gson.fromJson(jsonObject, tClass);
    }

    protected String decrypt(ChannelHandlerContext ctx, EncryptedBytes encryptedString) {

        if(!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.channel())) throw new NoSecretKeyException();

        SecretKey secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel());

        if (secretKey == null) {
            throw new NoSecretKeyException();
        }


        String decryptedJSON = null;


        try {
            decryptedJSON = EncryptionUtil.decrypt(encryptedString, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        return decryptedJSON;
    }

}
