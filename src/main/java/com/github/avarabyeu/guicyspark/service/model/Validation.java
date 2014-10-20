package com.github.avarabyeu.guicyspark.service.model;

import java.util.Date;

/**
 * Validation table model
 *
 * @author Andrei Varabyeu
 */
public class Validation {

    private Integer id;
    private Date date;
    private String error;
    private String url;
    private Status status;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static enum Status {
        OK,
        FAILED,
        UNDEFINED
    }
}
