package com.github.avarabyeu.guicyspark.service;

import com.github.avarabyeu.guicyspark.service.model.Validation;

import java.util.List;
import java.util.Map;

/**
 * Validation business logic interface
 *
 * @author Andrei Varabyeu
 */
public interface ValidationService {

    /**
     * Submits url for validation
     *
     * @param urls - URLs to be validated
     * @return Map<URL,Validation Request ID>
     */
    Map<String, Integer> askForValidation(List<String> urls);

    /**
     * Returns validation by ID
     *
     * @param id Validation Request ID
     * @return Validation Results
     */
    Validation getValidation(Integer id);

    /**
     * Returns all validations
     *
     * @return All validations
     */
    List<Validation> getValidations();
}
