package com.github.fernthedev.lightchat.core.codecs

import com.github.fernthedev.lightchat.core.codecs.json.FastJSONHandler
import com.github.fernthedev.lightchat.core.codecs.json.GSONHandler
import java.util.*

/**
 * Shortcut for json handlers
 */
object Codecs {
    private val jsonHandlerMap: MutableMap<String, JSONHandler> = HashMap()
    var GSON_STR = "GSON"
    val GSON: JSONHandler = GSONHandler.Companion.INSTANCE
    var FASTSON_STR = "ALIBABA_FASTJSON"
    val ALIBABA_FASTJSON: JSONHandler = FastJSONHandler()

    init {
        registerJsonHandler(GSON_STR, GSON)
        registerJsonHandler(FASTSON_STR, ALIBABA_FASTJSON)
    }

    /**
     * Gets the json handler
     * @param json gets converted to lowercase to avoid case-sensitive scenarios
     * @return
     */
    @kotlin.jvm.JvmStatic
    fun getJsonHandler(json: String): JSONHandler? {
        return jsonHandlerMap[json.lowercase(Locale.getDefault())]
    }

    /**
     *
     * @param json The name of the handler
     * @param jsonHandler The JSON handler should act as a static utility, avoiding per-instance scenarios such as server-access code.
     * @return The name being used in the map (turns lowercase)
     */
    fun registerJsonHandler(json: String, jsonHandler: JSONHandler): String {
        val j = json.lowercase(Locale.getDefault())
        jsonHandlerMap[j] = jsonHandler
        return j
    }
}