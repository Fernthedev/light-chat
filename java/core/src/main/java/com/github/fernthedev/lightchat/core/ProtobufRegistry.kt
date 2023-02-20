package com.github.fernthedev.lightchat.core

import com.google.protobuf.Message
import com.google.protobuf.MessageLite
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream

object ProtobufRegistry {

    private val map: MutableMap<String, (bytes: InputStream) -> MessageLite> = HashMap()


    fun <T: Message> addMessage(messageLite: T) {
        map[messageLite.descriptorForType.fullName] = {bytes -> messageLite.parserForType.parseFrom(bytes) }
    }

    fun <T: MessageLite> decode(name: String, bytes: ByteArray): T? {
        return map[name]?.invoke(ByteArrayInputStream(bytes)) as? T
    }
    fun <T: MessageLite> decode(name: String, bytes: ByteBuf): T? {
        return map[name]?.invoke(ByteBufInputStream(bytes)) as? T
    }

}