package com.skylin.uav.drawforterrain.util;

import java.util.concurrent.ScheduledFuture;

import sjj.schedule.Pool;

/**
 * Created by sjj on 2017/5/25.
 */

public class Timer {
    private final Runnable runnable;
    private ScheduledFuture<?> scheduledFuture;

    public Timer(Runnable runnable) {
        this.runnable = runnable;
    }

    public synchronized void start(long initialDelay) {
        stop();
        scheduledFuture = Pool.schedule(runnable, initialDelay);
    }

    public synchronized void start(long initialDelay, long delay) {
        stop();
        scheduledFuture = Pool.scheduleWithFixedDelay(runnable, initialDelay, delay);
    }

    public synchronized void stop() {
        ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        this.scheduledFuture = null;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }
}
