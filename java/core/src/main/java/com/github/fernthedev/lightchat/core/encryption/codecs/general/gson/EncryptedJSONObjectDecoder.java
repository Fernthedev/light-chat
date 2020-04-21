package com.github.fernthedev.lightchat.core.encryption.codecs.general.gson;

import com.github.fernthedev.lightchat.core.PacketRegistry;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.encryption.EncryptedBytes;
import com.github.fernthedev.lightchat.core.encryption.EncryptedPacketWrapper;
import com.github.fernthedev.lightchat.core.encryption.PacketWrapper;
import com.github.fernthedev.lightchat.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.lightchat.core.encryption.RSA.NoSecretKeyException;
import com.github.fernthedev.lightchat.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.lightchat.core.encryption.codecs.JSONHandler;
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.lightchat.core.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts encrypted json to a decrypted object
 */
@ChannelHandler.Sharable
public class EncryptedJSONObjectDecoder extends StringDecoder {

    private final JSONHandler jsonHandler;

    protected IEncryptionKeyHolder encryptionKeyHolder;
    protected Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public EncryptedJSONObjectDecoder(IEncryptionKeyHolder encryptionKeyHolder, JSONHandler jsonHandler) {
        this(Charset.defaultCharset(), encryptionKeyHolder, jsonHandler);
    }

    /**
     * Creates a new instance with the specified character set.
     *
     * @param charset
     */
    public EncryptedJSONObjectDecoder(Charset charset, IEncryptionKeyHolder encryptionKeyHolder, JSONHandler jsonHandler) {
        super(charset);
        this.charset = charset;
        this.encryptionKeyHolder = encryptionKeyHolder;
        this.jsonHandler = jsonHandler;
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
        StaticHandler.getCore().getLogger().debug("Decoding the string {}", decodedStr);
        PacketWrapper<?> packetWrapper = jsonHandler.fromJson(decodedStr, PacketWrapper.class);

        String decryptedJSON;

        try {
            if (packetWrapper.encrypt()) {
                packetWrapper = jsonHandler.fromJson(decodedStr, EncryptedPacketWrapper.class);

                EncryptedBytes encryptedBytes = jsonHandler.fromJson(packetWrapper.getJsonObject(), EncryptedBytes.class);
                decryptedJSON = decrypt(ctx, (encryptedBytes));
            } else {
                packetWrapper = jsonHandler.fromJson(decodedStr, UnencryptedPacketWrapper.class);
                decryptedJSON = packetWrapper.getJsonObject();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse string: " + decodedStr, e);
        }

        out.add(getParsedObject(packetWrapper.getPacketIdentifier(), decryptedJSON, packetWrapper.getPacketId()));
    }

    /**
     * Converts the JSON Object into it's former instance by providing the class name
     *
     * @param jsonObject
     * @return
     */
    public Pair<? extends Packet, Integer> getParsedObject(String packetIdentifier, String jsonObject, int packetId) {
        Class<? extends Packet> aClass = PacketRegistry.getPacketClassFromRegistry(packetIdentifier);

        try {
            return new ImmutablePair<>(jsonHandler.fromJson(jsonObject, aClass), packetId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Attempting to parse packet " + packetIdentifier + " (" + aClass.getName() + ") with string\n" + jsonObject, e);
        }
    }

    protected String decrypt(ChannelHandlerContext ctx, EncryptedBytes encryptedString) {

        if (!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.channel())) throw new NoSecretKeyException();

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
