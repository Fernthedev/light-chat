package com.github.fernthedev.lightchat.core.encryption

import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.util.asBytesArrayFast
import com.google.gson.annotations.SerializedName
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
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
    val packet: AcceptablePacketTypes,
    val encrypt: Boolean,
    val id: Int = -1, // only used in decode
) {
    private lateinit var packetWrapperCache: PacketWrapper
    private lateinit var packetWrapperJSON: ByteBuf

    internal fun packetWrapper(
        toBytes: (p: AcceptablePacketTypes) -> ByteArray,
        packetId: Int,
        secretKey: SecretKey?,
        cipher: Cipher,
        random: SecureRandom?
    ): Pair<PacketWrapper, ByteBuf> {
        if (this::packetWrapperCache.isInitialized) {
            return packetWrapperCache to packetWrapperJSON
        }

        packetWrapperCache = if (encrypt) {
            requireNotNull(secretKey) { "Can't encrypt with null secret key!" }
            requireNotNull(random) { "Can't encrypt with null random!" }
            val bytes = EncryptionUtil.encrypt(toBytes(packet), secretKey, cipher, random)

            PacketWrapper.encrypted(bytes, packet.packetName, packetId)
        } else {
            PacketWrapper.plain(packet, toBytes(packet), packetId)
        }

        packetWrapperJSON = packetWrapperCache.encode()

        return packetWrapperCache to packetWrapperJSON
    }
}

fun PacketJSON.transport(encrypt: Boolean = true): PacketTransporter {
    return PacketTransporter(this, encrypt)
}

enum class PacketType(val i: Byte) {
    UNKNOWN(-1),
    JSON(0), // 0
    PROTOBUF(1) // 1
}

internal data class PacketWrapper internal constructor(
    var jsonObject: ByteArray,
    var packetIdentifier: String,
    /**
     * For packet order
     */
    val packetId: Int = 0,

    @SerializedName("ENCRYPT")
    val encrypt: Boolean,

    val packetType: PacketType
) : Serializable {

    companion object {
        fun encrypted(
            encryptedBytes: EncryptedBytes,
            packetName: String,
            packetId: Int,
        ): PacketWrapper {
            return PacketWrapper(
                encryptedBytes.encode().asBytesArrayFast(),
                packetName,
                packetId,
                encrypt = true,
                packetType = PacketType.JSON
            )
        }

        fun plain(jsonObject: AcceptablePacketTypes, byteArray: ByteArray, packetId: Int): PacketWrapper {
            return PacketWrapper(
                byteArray,
                jsonObject.packetName,
                packetId,
                encrypt = false,
                packetType = PacketType.JSON
            )
        }

        fun decode(buf: ByteBuf): PacketWrapper {


            val encrypt = buf.readBoolean()
            val id = buf.readInt()
            val packetType = buf.readByte()
            val packetTypeEnum = PacketType.values().find { it.i == packetType } ?: PacketType.UNKNOWN

            val identifierSize = buf.readInt()
            val identifier = buf.readBytes(identifierSize)

            val jsonObjectSize = buf.readInt()
            val jsonObject = buf.readBytes(jsonObjectSize)

            return PacketWrapper(
                jsonObject = jsonObject.asBytesArrayFast(),
                packetId = id,
                packetIdentifier = identifier.toString(Charsets.UTF_8),
                encrypt = encrypt,
                packetType = packetTypeEnum
            )
        }
    }

    fun encode(): ByteBuf {
        val identifier = packetIdentifier.toByteArray()

        // in respective order
        val stream = Unpooled.buffer(1 + 4 + 1 + 4 + identifier.size + 4 + jsonObject.size)




        stream.writeBoolean(encrypt)
        stream.writeInt(packetId) // BigEndian
        stream.writeByte(packetType.i.toInt())

        stream.writeInt(identifier.size)
        stream.writeBytes(identifier)

        stream.writeInt(jsonObject.size)
        stream.writeBytes(jsonObject)

        return stream
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PacketWrapper) return false

        if (!jsonObject.contentEquals(other.jsonObject)) return false
        if (packetIdentifier != other.packetIdentifier) return false
        if (packetId != other.packetId) return false
        if (encrypt != other.encrypt) return false
        if (packetType != other.packetType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = jsonObject.contentHashCode()
        result = 31 * result + packetIdentifier.hashCode()
        result = 31 * result + packetId
        result = 31 * result + encrypt.hashCode()
        result = 31 * result + packetType.hashCode()
        return result
    }

}