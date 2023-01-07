package com.github.fernthedev.lightchat.core.encryption.RSA;

import com.github.fernthedev.lightchat.core.packets.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public interface IEncryptionKeyHolder {

//    @NonNull
//    PrivateKey getPrivateKey(ChannelHandlerContext ctx, Channel channel);
//
//    @NonNull PublicKey getPublicKey(ChannelHandlerContext ctx, Channel channel);

    SecretKey getSecretKey(ChannelHandlerContext ctx, Channel channel);

    Cipher getEncryptCipher(ChannelHandlerContext ctx, Channel channel);

    Cipher getDecryptCipher(ChannelHandlerContext ctx, Channel channel);

    /**
     * Should be a secure random with the seed based on the key
     * @param ctx
     * @param channel
     * @return
     */
    SecureRandom getSecureRandom(ChannelHandlerContext ctx, Channel channel);

    boolean isEncryptionKeyRegistered(ChannelHandlerContext ctx, Channel channel);

    Pair<Integer, Long> getPacketId(Class<? extends Packet> clazz, ChannelHandlerContext ctx, Channel channel);
}
