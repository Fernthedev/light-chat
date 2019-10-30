package com.github.fernthedev.server;

import com.github.fernthedev.universal.encryption.RSA.IEncryptionKeyHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.NonNull;

import javax.crypto.SecretKey;

public class EncryptionKeyFinder implements IEncryptionKeyHolder {

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
        return PlayerHandler.socketList.get(channel).getSecretKey();
    }

    @Override
    public boolean isEncryptionKeyRegistered(ChannelHandlerContext ctx, Channel channel) {
        return PlayerHandler.socketList.get(channel).getSecretKey() != null;
    }
}
