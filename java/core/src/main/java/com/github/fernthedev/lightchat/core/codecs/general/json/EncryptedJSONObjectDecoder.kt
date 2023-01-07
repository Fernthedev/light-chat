package com.github.fernthedev.lightchat.core.codecs.general.json;

import com.github.fernthedev.lightchat.core.PacketRegistry;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.codecs.JSONHandler;
import com.github.fernthedev.lightchat.core.encryption.EncryptedBytes;
import com.github.fernthedev.lightchat.core.encryption.EncryptedPacketWrapper;
import com.github.fernthedev.lightchat.core.encryption.PacketWrapper;
import com.github.fernthedev.lightchat.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.lightchat.core.encryption.RSA.NoSecretKeyException;
import com.github.fernthedev.lightchat.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.util.ExceptionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * Converts encrypted json to a decrypted object
 */
@ChannelHandler.Sharable
public class EncryptedJSONObjectDecoder extends MessageToMessageDecoder<String> {

    private final JSONHandler jsonHandler;

    protected IEncryptionKeyHolder encryptionKeyHolder;

    /**
     * Creates a new instance with the specified character set.
     *
     */
    public EncryptedJSONObjectDecoder(IEncryptionKeyHolder encryptionKeyHolder, JSONHandler jsonHandler) {
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
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        StaticHandler.getCore().getLogger().debug("Decoding the string {}", msg);
        PacketWrapper packetWrapper = jsonHandler.fromJson(msg, PacketWrapper.class);

        String decryptedJSON;

        try {
            if (packetWrapper.encrypt()) {
                packetWrapper = jsonHandler.fromJson(msg, EncryptedPacketWrapper.class);

                EncryptedBytes encryptedBytes = jsonHandler.fromJson(packetWrapper.getJsonObject(), EncryptedBytes.class);
                decryptedJSON = decrypt(ctx, encryptedBytes);
            } else {
                packetWrapper = jsonHandler.fromJson(msg, UnencryptedPacketWrapper.class);
                decryptedJSON = packetWrapper.getJsonObject();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse string: " + msg, e);
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

        @NonNull Cipher decryptCipher = encryptionKeyHolder.getDecryptCipher(ctx, ctx.channel());

        SecureRandom random = encryptionKeyHolder.getSecureRandom(ctx, ctx.channel());

        if (secretKey == null) {
            throw new NoSecretKeyException();
        }


        String decryptedJSON;


        try {
            decryptedJSON = EncryptionUtil.decrypt(encryptedString, secretKey, decryptCipher, random);
        } catch (BadPaddingException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
            throw ExceptionUtil.throwParsePacketException(e, Arrays.toString(encryptedString.getData()));
        }


        return decryptedJSON;
    }

}
