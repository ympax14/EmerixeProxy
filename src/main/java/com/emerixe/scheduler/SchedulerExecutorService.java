package com.emerixe.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerExecutorService {
    private final ScheduledExecutorService scheduler;

    public SchedulerExecutorService(Integer threads) {
        scheduler = Executors.newScheduledThreadPool(threads); // threads partagés
    }

    public void addTask(Runnable task, Integer delay, TimeUnit unit) {
        scheduler.schedule(task, delay, unit);
    }

    public void stop() {
        scheduler.shutdown();
    }
}