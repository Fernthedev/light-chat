package com.github.fernthedev.lightchat.core.encryption

import com.github.fernthedev.lightchat.core.util.asBytesArrayFast
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

data class EncryptedBytes(
    val data: ByteArray,
    val nonce: ByteArray
) {

    companion object {
        fun decode(buf: ByteBuf): EncryptedBytes {
            val dataSize = buf.readInt()
            val data = buf.readSlice(dataSize)

            val nonceSize = buf.readInt()
            val nonce = buf.readSlice(nonceSize)

            return EncryptedBytes(
                data = data.asBytesArrayFast(),
                nonce = nonce.asBytesArrayFast()
            )
        }
    }

    fun encode(): ByteBuf {
        val byteBuf = Unpooled.buffer(4 + data.size + 4 + nonce.size)
        byteBuf.writeInt(data.size)
        byteBuf.writeBytes(data)

        byteBuf.writeInt(nonce.size)
        byteBuf.writeBytes(nonce)

        return byteBuf
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedBytes) return false

        if (!data.contentEquals(other.data)) return false
        if (!nonce.contentEquals(other.nonce)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + nonce.contentHashCode()
        return result
    }


}