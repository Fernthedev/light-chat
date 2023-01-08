package com.github.fernthedev.lightchat.core.codecs.general.json

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.github.fernthedev.lightchat.core.encryption.*
import com.github.fernthedev.lightchat.core.encryption.RSA.IEncryptionKeyHolder
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

/**
 * Converts an object to an encrypted json
 */
@Sharable
class EncryptedJSONObjectEncoder(
    private var encryptionKeyHolder: IEncryptionKeyHolder,
    private val jsonHandler: JSONHandler
) : MessageToMessageEncoder<PacketTransporter>() {


    /**
     * Encode from one message to an other. This method will be called for each written message that can be handled
     * by this encoder.
     *
     * @param ctx the [ChannelHandlerContext] which this [MessageToMessageEncoder] belongs to
     * @param msg the message to encode to an other one
     * @param out the [List] into which the encoded msg should be added
     * needs to do some kind of aggregation
     * @throws Exception is thrown if an error occurs
     */
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: PacketTransporter, out: MutableList<Any>) {
        val cipher = encryptionKeyHolder.getEncryptCipher(ctx, ctx.channel())



        val packetWrapper = if (!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.channel())) {
            require(!msg.encrypt) { "Encryption is not setup yet!" }

            msg.packetWrapper(
                jsonHandler,
                encryptionKeyHolder.getPacketId(msg.packet.javaClass, ctx, ctx.channel()).first,
                null,
                cipher,
                null
            )
        } else {
            val secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel())
            val secureRandom = encryptionKeyHolder.getSecureRandom(ctx, ctx.channel())
            msg.packetWrapper(
                jsonHandler,
                encryptionKeyHolder.getPacketId(msg.packet.javaClass, ctx, ctx.channel()).first,
                secretKey,
                cipher,
                secureRandom
            )
        }

        out.add(packetWrapper.second)


        if (StaticHandler.isDebug()) StaticHandler.core.logger.debug(
            "Sending {}",
            packetWrapper.second
        )
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        StaticHandler.core.logger.error(cause.message, cause)
        super.exceptionCaught(ctx, cause)
    }
}