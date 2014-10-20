package com.github.avarabyeu.guicyspark.controller;

import com.github.avarabyeu.guicyspark.service.ValidationService;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import com.google.common.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import spark.servlet.SparkApplication;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_ACCEPTABLE;
import static spark.Spark.*;

/**
 * @author Andrei Varabyeu
 */
class ValidationController implements SparkApplication {


    @Inject
    private Transformer transformer;

    @Inject
    private ValidationService validationService;

    @Override
    public void init() {

        post("/rest/v1/validation", (request, response) -> {
            Map<String, List<String>> urls = transformer.unrender(request.body(), new TypeToken<Map<String, List<String>>>() {
            }.getType());
            response.status(HTTP_CREATED);
            return ImmutableMap.builder().put("urls", validationService.
                    askForValidation(urls.get("urls"))).build();

        }, transformer);


        get("/rest/v1/validation/:id", (request, response) -> validationService.getValidation(Integer.valueOf(request.params(":id"))), transformer);

        get("rest/v1/validation", (request, response) -> validationService.getValidations(), transformer);

        after((request, response) -> response.type(MediaType.JSON_UTF_8.toString()));

        exception(IllegalArgumentException.class, (e, request, response) -> {
            response.status(HTTP_NOT_ACCEPTABLE);
            response.body(e.getMessage());
        });

        exception(MalformedJsonException.class, (e, request, response) -> {
            response.status(HTTP_NOT_ACCEPTABLE);
            response.body(e.getMessage());
        });



    }
}
