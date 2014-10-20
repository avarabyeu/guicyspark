package com.github.avarabyeu.guicyspark.service;

import com.github.avarabyeu.guicyspark.service.model.Validation;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Andrei Varabyeu
 */
public class ValidationWorker extends AbstractScheduledService {


    public static final int INITIAL_DELAY = 5;

    private ListeningExecutorService validators = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    @Inject
    private ValidationDao validationDao;

    @Inject
    @Named("validation.worker.delay")
    private Long validationWorkerDelay;


    @Override
    protected void runOneIteration() throws Exception {
        List<Validation> validationRequests = validationDao.findByStatus(Validation.Status.UNDEFINED.toString());
        validationRequests.stream().
                forEach((validation) -> validators.submit(new Validator(validation)));
        validators.awaitTermination(20, TimeUnit.SECONDS);
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(INITIAL_DELAY, validationWorkerDelay, TimeUnit.SECONDS);
    }

    public class Validator implements Runnable {

        private Validation validation;

        public Validator(Validation validation) {
            this.validation = validation;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(validation.getUrl()).openConnection();
                urlConnection.connect();
                if (HttpURLConnection.HTTP_OK == urlConnection.getResponseCode()) {
                    validation.setStatus(Validation.Status.OK);
                } else {
                    validation.setStatus(Validation.Status.FAILED);
                    validation.setError(urlConnection.getResponseMessage());
                }

            } catch (UnknownHostException e) {
                validation.setStatus(Validation.Status.FAILED);
                validation.setError("Unknown host");
            } catch (IOException e) {
                validation.setStatus(Validation.Status.FAILED);
                validation.setError(e.getMessage());
            }
            validationDao.updateValidation(validation);
        }
    }


    @Override
    protected void shutDown() throws Exception {
        validators.shutdownNow();
        validators.awaitTermination(30, TimeUnit.SECONDS);
    }

}
