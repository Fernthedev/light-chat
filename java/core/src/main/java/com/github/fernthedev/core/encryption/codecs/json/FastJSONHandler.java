package com.github.fernthedev.core.encryption.codecs.json;

import com.alibaba.fastjson.JSON;
import com.github.fernthedev.core.api.APIUsage;
import com.github.fernthedev.core.encryption.codecs.JSONHandler;

@APIUsage
public class FastJSONHandler implements JSONHandler {
    @Override
    public <T> T fromJson(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
    }

    @Override
    public String toJson(Object src) {
        return JSON.toJSONString(src);
    }
}
