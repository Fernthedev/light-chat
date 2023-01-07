package com.github.fernthedev.lightchat.core.codecs.json

import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

@APIUsage
class GSONHandler private constructor() : JSONHandler {
    @Throws(JsonSyntaxException::class)
    override fun <T> fromJson(decodedStr: String, packetWrapperClass: Class<T>): T {
        return gson.fromJson(decodedStr, packetWrapperClass)
    }

    override fun toJson(msg: Any?): String {
        return gson.toJson(msg)
    }

    companion object {
        private val gson = Gson()
        val INSTANCE = GSONHandler()
    }
}