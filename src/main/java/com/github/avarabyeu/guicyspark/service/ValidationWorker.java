package com.github.avarabyeu.guicyspark.service;

import com.github.avarabyeu.guicyspark.service.model.Validation;
import com.github.avarabyeu.wills.WillExecutorService;
import com.github.avarabyeu.wills.WillExecutors;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledService which performs validation with fixed delay schedule
 * Obtains list of undefined (submitted) validation requests and processes them in several threads
 *
 * @author Andrei Varabyeu
 */
public class ValidationWorker extends AbstractScheduledService {


    /* Initial scheduler delay. Just to make sure all components started up */
    private static final int INITIAL_DELAY = 5;

    private WillExecutorService validators;

    @Inject
    private ValidationDao validationDao;

    @Inject
    @Named("validation.worker.delay")
    private Long validationWorkerDelay;

    @Inject
    @Named("validation.worker.chunk.count")
    private Integer chunksCount;

    @Override
    protected void runOneIteration() throws Exception {
        List<Validation> validationRequests = validationDao.findByStatus(Validation.Status.UNDEFINED.toString(), chunksCount);

        /* count down to wait current iteration for validations completion */
        CountDownLatch countDown = new CountDownLatch(validationRequests.size());
        validationRequests.stream().
                forEach((validation) -> validators.submit(new Validator(validation)).whenDone(result -> countDown.countDown()));

        /* blocks current thread until all validations are completed */
        countDown.await();
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
            System.out.println("finished");
        }
    }


    @Override
    protected void startUp() throws Exception {
        validators = WillExecutors.willDecorator(Executors.newFixedThreadPool(chunksCount));
    }

    @Override
    protected void shutDown() throws Exception {
        validators.shutdownNow();
        validators.awaitTermination(30, TimeUnit.SECONDS);
    }

}
