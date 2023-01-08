package com.github.fernthedev.lightchat.core.encryption

import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.packets.Packet
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey

/**
 * Holds the packet before it gets wrapped and sent to Netty
 * This should improve drastically excessive JSON serialization and memory usage
 * by cache
 */
class PacketTransporter
@JvmOverloads constructor(
    val packet: Packet,
    val encrypt: Boolean,
    val id: Int = -1, // only used in decode
) {
    private lateinit var packetWrapperCache: PacketWrapper
    private lateinit var packetWrapperJSON: String

    internal fun packetWrapper(
        jsonHandler: JSONHandler,
        packetId: Int,
        secretKey: SecretKey?,
        cipher: Cipher,
        random: SecureRandom?
    ): Pair<PacketWrapper, String> {
        if (this::packetWrapperCache.isInitialized) {
            return packetWrapperCache to packetWrapperJSON
        }

        packetWrapperCache = if (encrypt) {
            requireNotNull(secretKey) { "Can't encrypt with null secret key!" }
            requireNotNull(random) { "Can't encrypt with null random!" }
            val bytes = EncryptionUtil.encrypt(jsonHandler.toJson(packet), secretKey, cipher, random)

            PacketWrapper.encrypted(bytes, packet.packetName, jsonHandler, packetId)
        } else {
            PacketWrapper.plain(packet, jsonHandler, packetId)
        }

        packetWrapperJSON = jsonHandler.toJson(packetWrapperCache)

        return packetWrapperCache to packetWrapperJSON
    }
}

fun Packet.transport(encrypt: Boolean = true): PacketTransporter {
    return PacketTransporter(this, encrypt)
}

internal data class PacketWrapper internal constructor(
    var jsonObject: String,
    var packetIdentifier: String,
    /**
     * For packet order
     */
    val packetId: Int = 0,

    @SerializedName("ENCRYPT")
    val encrypt: Boolean,
) : Serializable {

    companion object {
        fun encrypted(
            encryptedBytes: EncryptedBytes,
            packetName: String,
            handler: JSONHandler,
            packetId: Int
        ): PacketWrapper {
            return PacketWrapper(handler.toJson(encryptedBytes), packetName, packetId, encrypt = true)
        }

        fun plain(jsonObject: Packet, jsonHandler: JSONHandler, packetId: Int): PacketWrapper {
            return PacketWrapper(
                jsonHandler.toJson(jsonObject),
                jsonObject.packetName,
                packetId,
                encrypt = false
            )
        }
    }
}