package com.github.fernthedev.lightchat.core.codecs.general.json

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.github.fernthedev.lightchat.core.encryption.*
import com.github.fernthedev.lightchat.core.encryption.RSA.IEncryptionKeyHolder
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.util.ExceptionUtil
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
        val secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel())
        val cipher = encryptionKeyHolder.getEncryptCipher(ctx, ctx.channel())
        val secureRandom = encryptionKeyHolder.getSecureRandom(ctx, ctx.channel())

        val packetWrapper = msg.packetWrapper(jsonHandler, encryptionKeyHolder.getPacketId(msg.packet.javaClass, ctx, ctx.channel()).key, secretKey, cipher, secureRandom)

        out.add(packetWrapper.second)


        if (StaticHandler.isDebug()) StaticHandler.core.logger.debug(
            "Sending {}",
            packetWrapper.second
        )
    }

    private fun encrypt(ctx: ChannelHandlerContext, decryptedString: String?): EncryptedBytes {
        var fixedDecryptedString = decryptedString

        val secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel())
        val cipher = encryptionKeyHolder.getEncryptCipher(ctx, ctx.channel())
        val secureRandom = encryptionKeyHolder.getSecureRandom(ctx, ctx.channel())

        if (fixedDecryptedString.isNullOrEmpty()) fixedDecryptedString = ""

        val encryptedJSON: EncryptedBytes = try {
            EncryptionUtil.encrypt(fixedDecryptedString, secretKey, cipher, secureRandom)
        } catch (e: Exception) {
            if (StaticHandler.isDebug()) e.printStackTrace()
            throw ExceptionUtil.throwParsePacketException(e, fixedDecryptedString)
        }
        return encryptedJSON
    }
}