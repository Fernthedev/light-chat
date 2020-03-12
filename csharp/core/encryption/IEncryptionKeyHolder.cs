using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core.encryption
{
    interface IEncryptionKeyHolder
    {
        SecretKey getSecretKey(ChannelHandlerContext ctx, Channel channel);

        boolean isEncryptionKeyRegistered(ChannelHandlerContext ctx, Channel channel);

        Pair<Integer, Long> getPacketId(Class<? extends Packet> clazz, ChannelHandlerContext ctx, Channel channel);

    }
}
