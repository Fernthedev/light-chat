package com.github.fernthedev.lightchat.core.encryption.RSA

import com.github.fernthedev.lightchat.core.packets.Packet
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import org.apache.commons.lang3.tuple.Pair
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey

interface IEncryptionKeyHolder {
    //    @NonNull
    //    PrivateKey getPrivateKey(ChannelHandlerContext ctx, Channel channel);
    //
    //    @NonNull PublicKey getPublicKey(ChannelHandlerContext ctx, Channel channel);
    fun getSecretKey(ctx: ChannelHandlerContext, channel: Channel): SecretKey
    fun getEncryptCipher(ctx: ChannelHandlerContext, channel: Channel): Cipher
    fun getDecryptCipher(ctx: ChannelHandlerContext, channel: Channel): Cipher

    /**
     * Should be a secure random with the seed based on the key
     * @param ctx
     * @param channel
     * @return
     */
    fun getSecureRandom(ctx: ChannelHandlerContext, channel: Channel): SecureRandom
    fun isEncryptionKeyRegistered(ctx: ChannelHandlerContext, channel: Channel): Boolean
    fun getPacketId(clazz: Class<out Packet>, ctx: ChannelHandlerContext, channel: Channel): Pair<Int, Long>
}