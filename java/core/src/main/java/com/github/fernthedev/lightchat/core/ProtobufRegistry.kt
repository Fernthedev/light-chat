package com.github.fernthedev.lightchat.core

import com.google.protobuf.Message
import com.google.protobuf.MessageLite

object ProtobufRegistry {

    private val map: MutableMap<String, (bytes: ByteArray) -> MessageLite> = HashMap()


    fun <T: Message> addMessage(messageLite: T) {
        map[messageLite.descriptorForType.fullName] = {bytes -> messageLite.newBuilderForType().mergeFrom(bytes).build() }
    }

    fun <T: MessageLite> decode(name: String, bytes: ByteArray): T? {
        return map[name]?.invoke(bytes) as? T
    }

}