package com.github.fernthedev.lightchat.core.codecs.general.json

import com.github.fernthedev.lightchat.core.PacketJsonRegistry
import com.github.fernthedev.lightchat.core.ProtobufRegistry
import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.github.fernthedev.lightchat.core.encryption.EncryptedBytes
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.encryption.PacketType
import com.github.fernthedev.lightchat.core.encryption.PacketWrapper
import com.github.fernthedev.lightchat.core.encryption.rsa.IEncryptionKeyHolder
import com.github.fernthedev.lightchat.core.encryption.rsa.NoSecretKeyException
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.packets.PacketProto
import com.github.fernthedev.lightchat.core.util.ExceptionUtil
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.util.*

/**
 * Converts encrypted json to a decrypted object
 */
@Sharable
class EncryptedJSONObjectDecoder
/**
 * Creates a new instance with the specified character set.
 *
 */(private var encryptionKeyHolder: IEncryptionKeyHolder, private val jsonHandler: JSONHandler) :
    MessageToMessageDecoder<ByteBuf>() {
    /**
     * Returns a string list
     *
     * @param ctx
     * @param msg The data received
     * @param out The returned objects
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        try {
            val packetWrapper = PacketWrapper.decode(msg)
            val decryptedJSON = if (packetWrapper.encrypt) {
                val tempByteBuf = packetWrapper.jsonObject
                val encryptedBytes = EncryptedBytes.decode(tempByteBuf)
                Unpooled.wrappedBuffer(decrypt(ctx, encryptedBytes))
            } else {
                packetWrapper.jsonObject
            }


            val obj: PacketTransporter = when (packetWrapper.packetType) {
                PacketType.UNKNOWN -> TODO()
                PacketType.JSON -> getParsedObject(
                    packetWrapper.packetIdentifier,
                    decryptedJSON.toString(Charsets.UTF_8),
                    packetWrapper.packetId
                )

                PacketType.PROTOBUF -> PacketTransporter(
                    PacketProto(
                        ProtobufRegistry.decode(
                            packetWrapper.packetIdentifier,
                            decryptedJSON
                        )!!
                    ),
                    encrypt = packetWrapper.encrypt
                )
            }
            StaticHandler.core.logger.debug("Received {}", packetWrapper.packetIdentifier)
            out.add(obj)
        } catch (e: Exception) {
            throw IllegalArgumentException("Unable to parse packet: $msg", e)
        }
    }

    /**
     * Converts the JSON Object into it's former instance by providing the class name
     *
     * @param jsonObject
     * @return
     */
    private fun getParsedObject(packetIdentifier: String, jsonObject: String, packetId: Int): PacketTransporter {
        val aClass = PacketJsonRegistry.getPacketClassFromRegistry(packetIdentifier)
        return try {
            PacketTransporter(jsonHandler.fromJson(jsonObject, aClass), encrypt = false, id = packetId)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                """Attempting to parse packet $packetIdentifier (${aClass.name}) with string
$jsonObject""", e
            )
        }
    }

    private fun decrypt(ctx: ChannelHandlerContext, encryptedString: EncryptedBytes): ByteArray {
        if (!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.channel())) throw NoSecretKeyException()
        val secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel())
        val random = encryptionKeyHolder.getSecureRandom(ctx, ctx.channel())
        requireNotNull(secretKey)
        requireNotNull(random)

        val decryptCipher = encryptionKeyHolder.getDecryptCipher(ctx, ctx.channel())
        val decryptedJSON = try {
            EncryptionUtil.decrypt(encryptedString, secretKey, decryptCipher, encryptedString.nonce)
        } catch (e: Exception) {
            throw ExceptionUtil.throwParsePacketException(e, encryptedString.data.contentToString())
        }

        return decryptedJSON
    }
}