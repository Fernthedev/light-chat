package com.github.fernthedev.lightchat.core.packets.handshake

import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil
import com.github.fernthedev.lightchat.core.packets.PacketInfo
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.BadPaddingException
import javax.crypto.SecretKey

@PacketInfo(name = "KEY_RESPONSE_PACKET")
class KeyResponsePacket private constructor(private val secretKeyEncrypted: ByteArray) : PacketJSON() {


    constructor(secretKey: SecretKey, publicKey: PublicKey?) : this(
        RSAEncryptionUtil.encryptKey(
            secretKey, publicKey
        )!!
    )

    @Throws(InvalidKeyException::class, BadPaddingException::class, NoSuchAlgorithmException::class)
    fun getSecretKey(privateKey: PrivateKey): SecretKey {
        return RSAEncryptionUtil.decryptKey(secretKeyEncrypted, privateKey)
    }
}