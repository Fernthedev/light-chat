package com.github.fernthedev.lightchat.core.codecs.json;

import com.github.fernthedev.lightchat.core.api.APIUsage;
import com.github.fernthedev.lightchat.core.codecs.JSONHandler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@APIUsage
public class GSONHandler implements JSONHandler {
    private static final Gson gson = new Gson();

    public static final GSONHandler INSTANCE = new GSONHandler();

    private GSONHandler() {}

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    @Override
    public String toJson(Object src) {
        return gson.toJson(src);
    }
}
