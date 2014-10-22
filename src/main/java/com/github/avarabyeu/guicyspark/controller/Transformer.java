package com.github.avarabyeu.guicyspark.controller;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import spark.ResponseTransformer;

import javax.inject.Inject;
import java.lang.reflect.Type;

/**
 * Sparks ResponseTransformer with deserialization support
 * It's pretty strange that there is no any support for RequestTransformer (or smth else)
 * Thus, Transformer will be used for both serialization and deserialization
 *
 * @author Andrei Varabyeu
 */
class Transformer implements ResponseTransformer {

    private final Gson gson;

    @Inject
    Transformer(Gson gson) {
        this.gson = Preconditions.checkNotNull(gson);
    }

    @Override
    public String render(Object model) throws Exception {
        return null == model ? null : gson.toJson(model);
    }

    public <T> T unrender(String rendered, Class<T> type) {
        return gson.fromJson(rendered, type);
    }

    public <T> T unrender(String rendered, Type type) {
        return gson.fromJson(rendered, type);
    }
}
