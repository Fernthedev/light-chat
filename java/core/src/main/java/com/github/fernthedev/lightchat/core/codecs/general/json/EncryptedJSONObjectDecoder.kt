package com.github.fernthedev.lightchat.core.codecs.general.json

import com.github.fernthedev.lightchat.core.PacketRegistry
import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.github.fernthedev.lightchat.core.encryption.EncryptedBytes
import com.github.fernthedev.lightchat.core.encryption.PacketWrapper
import com.github.fernthedev.lightchat.core.encryption.RSA.IEncryptionKeyHolder
import com.github.fernthedev.lightchat.core.encryption.RSA.NoSecretKeyException
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.util.ExceptionUtil
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

/**
 * Converts encrypted json to a decrypted object
 */
@Sharable
class EncryptedJSONObjectDecoder
/**
 * Creates a new instance with the specified character set.
 *
 */(private var encryptionKeyHolder: IEncryptionKeyHolder, private val jsonHandler: JSONHandler) :
    MessageToMessageDecoder<String>() {
    /**
     * Returns a string list
     *
     * @param ctx
     * @param msg The data received
     * @param out The returned objects
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: String, out: MutableList<Any>) {
        StaticHandler.core.logger.debug("Decoding the string {}", msg)
        val packetWrapper = jsonHandler.fromJson(msg, PacketWrapper::class.java)
        val decryptedJSON: String

        try {
            decryptedJSON = if (packetWrapper.encrypt) {
                val encryptedBytes = jsonHandler.fromJson(packetWrapper.jsonObject, EncryptedBytes::class.java)
                decrypt(ctx, encryptedBytes)
            } else {
                packetWrapper.jsonObject
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Unable to parse string: $msg", e)
        }

        out.add(getParsedObject(packetWrapper.packetIdentifier, decryptedJSON, packetWrapper.packetId))
    }

    /**
     * Converts the JSON Object into it's former instance by providing the class name
     *
     * @param jsonObject
     * @return
     */
    fun getParsedObject(packetIdentifier: String?, jsonObject: String, packetId: Int): Pair<out Packet?, Int> {
        val aClass = PacketRegistry.getPacketClassFromRegistry(packetIdentifier)
        return try {
            ImmutablePair(jsonHandler.fromJson(jsonObject, aClass), packetId)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                """Attempting to parse packet $packetIdentifier (${aClass.name}) with string
$jsonObject""", e
            )
        }
    }

    private fun decrypt(ctx: ChannelHandlerContext, encryptedString: EncryptedBytes): String {
        if (!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.channel())) throw NoSecretKeyException()
        val secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel())
        val decryptCipher = encryptionKeyHolder.getDecryptCipher(ctx, ctx.channel())
        val random = encryptionKeyHolder.getSecureRandom(ctx, ctx.channel())
        val decryptedJSON: String = try {
            EncryptionUtil.decrypt(encryptedString, secretKey, decryptCipher, random)
        } catch (e: BadPaddingException) {
            throw ExceptionUtil.throwParsePacketException(e, encryptedString.data.contentToString())
        } catch (e: IOException) {
            throw ExceptionUtil.throwParsePacketException(e, encryptedString.data.contentToString())
        } catch (e: NoSuchAlgorithmException) {
            throw ExceptionUtil.throwParsePacketException(e, encryptedString.data.contentToString())
        } catch (e: InvalidKeyException) {
            throw ExceptionUtil.throwParsePacketException(e, encryptedString.data.contentToString())
        } catch (e: InvalidAlgorithmParameterException) {
            throw ExceptionUtil.throwParsePacketException(e, encryptedString.data.contentToString())
        } catch (e: IllegalBlockSizeException) {
            throw ExceptionUtil.throwParsePacketException(e, encryptedString.data.contentToString())
        }
        return decryptedJSON
    }
}