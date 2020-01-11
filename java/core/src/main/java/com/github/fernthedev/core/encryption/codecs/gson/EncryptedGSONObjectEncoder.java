package com.github.fernthedev.core.encryption.codecs.gson;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.encryption.EncryptedBytes;
import com.github.fernthedev.core.encryption.EncryptedPacketWrapper;
import com.github.fernthedev.core.encryption.PacketWrapper;
import com.github.fernthedev.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.core.encryption.codecs.AcceptablePacketTypes;
import com.github.fernthedev.core.encryption.codecs.LineEndStringEncoder;
import com.github.fernthedev.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.core.packets.Packet;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.NonNull;

import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Converts an object to a encrypted json
 */
@ChannelHandler.Sharable
public class EncryptedGSONObjectEncoder extends MessageToMessageEncoder<AcceptablePacketTypes> {

    private static final Gson gson = new Gson();
    private final LineEndStringEncoder encoder;

    protected IEncryptionKeyHolder encryptionKeyHolder;
    protected Charset charset;


    /**
     * Creates a new instance with the current system character set.
     */
    public EncryptedGSONObjectEncoder(IEncryptionKeyHolder encryptionKeyHolder) {
        this(Charset.defaultCharset(), encryptionKeyHolder);
    }

    /**
     * Creates a new instance with the specified character set.
     *
     * @param charset
     */
    public EncryptedGSONObjectEncoder(Charset charset, IEncryptionKeyHolder encryptionKeyHolder) {
        encoder = new LineEndStringEncoder(charset);
        this.charset = charset;
        this.encryptionKeyHolder = encryptionKeyHolder;
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
            String decryptedJSON = gson.toJson(msg);

            encoder.encode(ctx, decryptedJSON, out); // Just encodes the string

        } else {

            if (!(msg instanceof Packet)) {
                throw new IllegalArgumentException("The object was not a packet instance. ");
            }

            // Encrypting the data
            String decryptedJSON = gson.toJson(msg);
            EncryptedBytes encryptedBytes = encrypt(ctx, decryptedJSON);

            // Adds the encrypted json in the packet wrapper
            packetWrapper = new EncryptedPacketWrapper(encryptedBytes, (Packet) msg);
            String jsonPacketWrapper = gson.toJson(packetWrapper);

            // Encodes the string for sending
            encoder.encode(ctx, jsonPacketWrapper, out);

        }

        StaticHandler.getCore().getLogger().info("Sending {}", gson.toJson(packetWrapper));
    }

    public EncryptedBytes encrypt(ChannelHandlerContext ctx, String decryptedString) {

        if (decryptedString == null) return null;

        if (decryptedString.length() == 0) {
            return null;
        }


        @NonNull SecretKey secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel());

//        System.out.println("Encrypting the message");
        EncryptedBytes encryptedJSON = null;

        try {
            encryptedJSON = EncryptionUtil.encrypt(decryptedString, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedJSON;
    }
}
