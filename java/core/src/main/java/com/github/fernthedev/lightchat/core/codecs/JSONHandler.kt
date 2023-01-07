package com.github.fernthedev.lightchat.core.codecs

interface JSONHandler {
    fun <T> fromJson(decodedStr: String, packetWrapperClass: Class<T>): T
    fun toJson(msg: Any?): String
}