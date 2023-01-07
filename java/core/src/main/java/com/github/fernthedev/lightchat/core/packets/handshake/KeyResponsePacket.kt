package com.github.fernthedev.lightchat.core.packets.handshake

import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.PacketInfo
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.BadPaddingException
import javax.crypto.SecretKey

@PacketInfo(name = "KEY_RESPONSE_PACKET")
class KeyResponsePacket(secretKey: SecretKey, publicKey: PublicKey?) : Packet() {
    private val secretKeyEncrypted: ByteArray?

    init {
        secretKeyEncrypted = RSAEncryptionUtil.encryptKey(secretKey, publicKey)
    }

    @Throws(InvalidKeyException::class, BadPaddingException::class, NoSuchAlgorithmException::class)
    fun getSecretKey(privateKey: PrivateKey): SecretKey {
        return RSAEncryptionUtil.decryptKey(secretKeyEncrypted, privateKey)
    }
}