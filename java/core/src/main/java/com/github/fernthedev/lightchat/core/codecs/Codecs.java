package com.github.fernthedev.lightchat.core.codecs;

import com.github.fernthedev.lightchat.core.codecs.json.FastJSONHandler;
import com.github.fernthedev.lightchat.core.codecs.json.GSONHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Shortcut for json handlers
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Codecs {

    private static final Map<String, JSONHandler> jsonHandlerMap = new HashMap<>();

    public static String GSON_STR = "GSON";
    public static final JSONHandler GSON = new GSONHandler();

    public static String FASTSON_STR = "ALIBABA_FASTJSON";
    public static final JSONHandler ALIBABA_FASTJSON = new FastJSONHandler();

    static {
        registerJsonHandler(GSON_STR, GSON);
        registerJsonHandler(FASTSON_STR, ALIBABA_FASTJSON);
    }

    /**
     * Gets the json handler
     * @param json gets converted to lowercase to avoid case-sensitive scenarios
     * @return
     */
    public static JSONHandler getJsonHandler(String json) {
        return jsonHandlerMap.get(json.toLowerCase());
    }

    /**
     *
     * @param json The name of the handler
     * @param jsonHandler The JSON handler should act as a static utility, avoiding per-instance scenarios such as server-access code.
     * @return The name being used in the map (turns lowercase)
     */
    public static String registerJsonHandler(String json, JSONHandler jsonHandler) {
        String j = json.toLowerCase();

        jsonHandlerMap.put(j, jsonHandler);

        return j;
    }
}
