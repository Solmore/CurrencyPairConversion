package org.solmore;

import org.solmore.service.CurrencyService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {

    private final CurrencyService service;

    public TaskScheduler(CurrencyService service) {
        this.service = service;
    }

    public void scheduleValidationPairTask() {
        service.validationPair();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        long initialDelay = calculateInitialDelay();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(service::validationPair, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private long calculateInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).toMillis();
    }
}
