package com.github.fernthedev.universal.encryption.RSA;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.SecretKey;

public interface IEncryptionKeyHolder {

//    @NonNull
//    PrivateKey getPrivateKey(ChannelHandlerContext ctx, Channel channel);
//
//    @NonNull PublicKey getPublicKey(ChannelHandlerContext ctx, Channel channel);

    SecretKey getSecretKey(ChannelHandlerContext ctx, Channel channel);

    boolean isEncryptionKeyRegistered(ChannelHandlerContext ctx, Channel channel);
}
