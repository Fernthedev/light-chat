package com.github.fernthedev.server;

import com.github.fernthedev.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.core.packets.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.SecretKey;

@RequiredArgsConstructor
public class EncryptionKeyFinder implements IEncryptionKeyHolder {

    private final Server server;


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

    @Override
    public @NonNull SecretKey getSecretKey(ChannelHandlerContext ctx, Channel channel) {
        return server.getPlayerHandler().getChannelMap().get(channel).getSecretKey();
    }

    @Override
    public boolean isEncryptionKeyRegistered(ChannelHandlerContext ctx, Channel channel) {
        return server.getPlayerHandler().getChannelMap().get(channel).getSecretKey() != null;
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    @Override
    public Pair<Integer, Long> getPacketId(Class<? extends Packet> clazz, ChannelHandlerContext ctx, Channel channel) {
        return server.getPlayerHandler().getChannelMap().get(channel).getPacketId(clazz);
    }
}
