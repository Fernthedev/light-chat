using com.github.fernthedev.lightchat.core.packets;
using com.github.fernthedev.lightchat.core.util;
using DotNetty.Transport.Channels;
using System;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;

namespace com.github.fernthedev.lightchat.core.encryption
{
    public interface IEncryptionKeyHolder
    {
        RijndaelManaged getSecretKey(IChannelHandlerContext ctx, IChannel channel);

        bool isEncryptionKeyRegistered(IChannelHandlerContext ctx, IChannel channel);

        /**
         * Packet:[ID,lastPacketSentTime]
         */
        Tuple<int, long> getPacketId(GenericType<Packet> clazz, IChannelHandlerContext ctx, IChannel channel);

    }
}
