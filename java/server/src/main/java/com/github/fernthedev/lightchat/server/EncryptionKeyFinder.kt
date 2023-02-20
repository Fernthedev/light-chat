package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.encryption.rsa.IEncryptionKeyHolder
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey


class EncryptionKeyFinder(
    val server: Server
) : IEncryptionKeyHolder {

    //    @Override
    //    public @NonNull PrivateKey getPrivateKey(ChannelHandlerContext ctx, Channel channel) {
    ////        System.out.println("Getting private key for " + channel.remoteAddress());
    //        return PlayerHandler.socketList.get(channel).getCurrentKeyPair().getPrivate();
    //    }
    //
    //    @Override
    //    public @NonNull PublicKey getPublicKey(ChannelHandlerContext ctx, Channel channel) {
    ////        System.out.println("Getting public key for " + channel.remoteAddress());
    //        return PlayerHandler.socketList.get(channel).getCurrentKeyPair().getPublic();
    //    }
    override fun getSecretKey(ctx: ChannelHandlerContext, channel: Channel): SecretKey? {
        return server.playerHandler.channelMap[channel]!!.secretKey
    }

    override fun getEncryptCipher(ctx: ChannelHandlerContext, channel: Channel): ThreadLocal<Cipher> {
        return server.playerHandler.channelMap[channel]!!.encryptCipher
    }

    override fun getDecryptCipher(ctx: ChannelHandlerContext, channel: Channel): ThreadLocal<Cipher> {
        return server.playerHandler.channelMap[channel]!!.decryptCipher
    }

    override fun getSecureRandom(ctx: ChannelHandlerContext, channel: Channel): SecureRandom? {
        return server.playerHandler.channelMap[channel]!!.secureRandom
    }

    override fun isEncryptionKeyRegistered(ctx: ChannelHandlerContext, channel: Channel): Boolean {
        val clientConnection = server.playerHandler.channelMap[channel]!!

        return clientConnection.encryptionRegistered()
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    override fun getPacketId(clazz: Class<out AcceptablePacketTypes>, ctx: ChannelHandlerContext, channel: Channel): Pair<Int, Long> {
        return server.playerHandler.channelMap[channel]!!.getPacketId(clazz)
    }
}