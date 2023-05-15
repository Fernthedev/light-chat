package com.github.fernthedev.lightchat.core.codecs.json

import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.google.gson.JsonSyntaxException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@APIUsage
class MoshiHandler private constructor() : JSONHandler {
    @Throws(JsonSyntaxException::class)
    override fun <T> fromJson(decodedStr: String, packetWrapperClass: Class<T>): T? {
        return moshi.adapter(packetWrapperClass).fromJson(decodedStr)
    }

    override fun toJson(msg: Any?): String {
        if (msg == null) return "null"
        return moshi.adapter(msg.javaClass).toJson(msg)
    }

    companion object {
        private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val INSTANCE = MoshiHandler()
    }
}