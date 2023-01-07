package com.github.fernthedev.lightchat.core.codecs.json

import com.alibaba.fastjson.JSON
import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.codecs.JSONHandler

@APIUsage
class FastJSONHandler : JSONHandler {

    override fun <T> fromJson(decodedStr: String, packetWrapperClass: Class<T>): T {
        return JSON.parseObject(decodedStr, packetWrapperClass)
    }

    override fun toJson(src: Any?): String {
        return JSON.toJSONString(src)
    }
}