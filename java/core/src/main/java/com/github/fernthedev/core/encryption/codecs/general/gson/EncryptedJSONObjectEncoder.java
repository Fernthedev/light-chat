package com.github.fernthedev.core.encryption.codecs.general.gson;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.encryption.EncryptedBytes;
import com.github.fernthedev.core.encryption.EncryptedPacketWrapper;
import com.github.fernthedev.core.encryption.PacketWrapper;
import com.github.fernthedev.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.core.encryption.codecs.AcceptablePacketTypes;
import com.github.fernthedev.core.encryption.codecs.JSONHandler;
import com.github.fernthedev.core.encryption.codecs.LineEndStringEncoder;
import com.github.fernthedev.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.core.packets.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.NonNull;

import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Converts an object to an encrypted json
 */
@ChannelHandler.Sharable
public class EncryptedJSONObjectEncoder extends MessageToMessageEncoder<AcceptablePacketTypes> {

    private final JSONHandler jsonHandler;
    private final LineEndStringEncoder encoder;

    protected IEncryptionKeyHolder encryptionKeyHolder;
    protected Charset charset;


    /**
     * Creates a new instance with the current system character set.
     */
    public EncryptedJSONObjectEncoder(IEncryptionKeyHolder encryptionKeyHolder, JSONHandler jsonHandler) {
        this(Charset.defaultCharset(), encryptionKeyHolder, jsonHandler);
    }

    /**
     * Creates a new instance with the specified character set.
     *
     * @param charset
     */
    public EncryptedJSONObjectEncoder(Charset charset, IEncryptionKeyHolder encryptionKeyHolder, JSONHandler jsonHandler) {
        encoder = new LineEndStringEncoder(charset);
        this.charset = charset;
        this.encryptionKeyHolder = encryptionKeyHolder;
        this.jsonHandler = jsonHandler;
        StaticHandler.getCore().getLogger().debug("Using charset {} for encrypting", charset);
    }

    /**
     * Encode from one message to an other. This method will be called for each written message that can be handled
     * by this encoder.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageEncoder} belongs to
     * @param msg the message to encode to an other one
     * @param out the {@link List} into which the encoded msg should be added
     *            needs to do some kind of aggregation
     * @throws Exception is thrown if an error occurs
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, AcceptablePacketTypes msg, List<Object> out) throws Exception {

        PacketWrapper<?> packetWrapper;
        if (msg instanceof UnencryptedPacketWrapper) {
            packetWrapper = (PacketWrapper<?>) msg;
            String decryptedJSON = jsonHandler.toJson(msg);

            encoder.encode(ctx, decryptedJSON, out); // Just encodes the string

        } else {

            if (!(msg instanceof Packet)) {
                throw new IllegalArgumentException("The object was not a packet instance. ");
            }

            Packet packet = (Packet) msg;

            // Encrypting the data
            String decryptedJSON = jsonHandler.toJson(msg);
            EncryptedBytes encryptedBytes = encrypt(ctx, decryptedJSON);

            // Adds the encrypted json in the packet wrapper
            packetWrapper = new EncryptedPacketWrapper(encryptedBytes, packet, encryptionKeyHolder.getPacketId(packet.getClass(), ctx, ctx.channel()).getKey());
            String jsonPacketWrapper = jsonHandler.toJson(packetWrapper);

            // Encodes the string for sending
            encoder.encode(ctx, jsonPacketWrapper, out);

        }

//        StaticHandler.getCore().getLogger().debug("Sending {}", jsonHandler.toJson(packetWrapper));
    }

    public EncryptedBytes encrypt(ChannelHandlerContext ctx, String decryptedString) {

        if (decryptedString == null) return null;

        if (decryptedString.isEmpty()) {
            return null;
        }


        @NonNull SecretKey secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel());
        EncryptedBytes encryptedJSON = null;

        try {
            encryptedJSON = EncryptionUtil.encrypt(decryptedString, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedJSON;
    }
}
