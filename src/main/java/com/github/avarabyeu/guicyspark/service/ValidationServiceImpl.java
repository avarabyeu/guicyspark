package com.github.avarabyeu.guicyspark.service;

import com.github.avarabyeu.guicyspark.service.model.Validation;
import org.apache.ibatis.session.ExecutorType;
import org.mybatis.guice.transactional.Transactional;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Andrei Varabyeu
 */
public class ValidationServiceImpl implements ValidationService {

    @Inject
    private ValidationDao validationDao;

    /* This type of executor will reuse PreparedStatements */
    @Override
    @Transactional(executorType = ExecutorType.REUSE)
    public Map<String, Integer> askForValidation(List<String> urls) {
        return urls.stream().map((url) -> {
            Validation validation = new Validation();
            validation.setDate(new Date());
            validation.setUrl(validateUrl(url));
            validation.setStatus(Validation.Status.UNDEFINED);
            validationDao.insertValidation(validation);
            return validation;
        }).collect(Collectors.toMap(Validation::getUrl, Validation::getId));
    }

    @Override
    public Validation getValidation(Integer id) {
        return validationDao.findById(id);
    }

    @Override
    public List<Validation> getValidations() {
        return validationDao.findAll();
    }

    private String validateUrl(String url) {
        try {
            return new URL(url).toExternalForm();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("URL '%s' is invalid", url));
        }
    }
}
