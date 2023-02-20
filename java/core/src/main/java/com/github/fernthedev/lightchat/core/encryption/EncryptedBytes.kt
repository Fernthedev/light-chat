package com.github.fernthedev.lightchat.core.encryption

import com.github.fernthedev.lightchat.core.util.asBytesArrayFast
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

data class EncryptedBytes(val data: ByteArray, val params: ByteArray, val paramAlgorithm: String) {

    companion object {
        fun decode(buf: ByteBuf): EncryptedBytes {
            val dataSize = buf.readInt()
            val data = buf.readSlice(dataSize)

            val paramsSize = buf.readInt()
            val params = buf.readSlice(paramsSize)

            val paramsAlgorithmSize = buf.readInt()
            val paramsAlgorithm = buf.readSlice(paramsAlgorithmSize).toString(Charsets.UTF_8)

            return EncryptedBytes(
                data = data.asBytesArrayFast(),
                params = params.asBytesArrayFast(),
                paramAlgorithm = paramsAlgorithm
            )
        }
    }

    fun encode(): ByteBuf {
        val algorithm = paramAlgorithm.toByteArray()
        val byteBuf = Unpooled.buffer(4 + data.size + 4 + params.size + 4 + algorithm.size)
        byteBuf.writeInt(data.size)
        byteBuf.writeBytes(data)

        byteBuf.writeInt(params.size)
        byteBuf.writeBytes(params)

        byteBuf.writeInt(algorithm.size)
        byteBuf.writeBytes(algorithm)

        return byteBuf
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedBytes) return false

        if (!data.contentEquals(other.data)) return false
        if (!params.contentEquals(other.params)) return false
        if (paramAlgorithm != other.paramAlgorithm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + params.contentHashCode()
        result = 31 * result + paramAlgorithm.hashCode()
        return result
    }
}