package com.skylin.mavlink.utils;

import android.support.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sjj on 2017/5/25.
 */

public class Timer {
    private Scheduler scheduler;
    private final Runnable runnable;
    private final long period;
    private Disposable scheduledFuture;

    public Timer(Runnable runnable) {
        this(runnable, -1);
    }

    public Timer(Runnable runnable , long period) {
        this(Schedulers.computation(), runnable, period);
    }

    public Timer(@NonNull Scheduler scheduler, Runnable runnable , long period) {
        this.scheduler = scheduler;
        this.runnable = runnable;
        this.period = period;
    }

    public synchronized void start(long initialDelay) {
        stop();
        if (period > 0) {
            start(initialDelay, period);
        } else {
            scheduledFuture = Pool.submit(scheduler,runnable, initialDelay);
        }
    }

    public synchronized void start(long initialDelay, long delay) {
        stop();
        scheduledFuture = Pool.submit(scheduler,runnable, initialDelay, delay);
    }

    public synchronized void stop() {
        Disposable scheduledFuture = this.scheduledFuture;
        this.scheduledFuture = null;
        if (scheduledFuture != null) {
            scheduledFuture.dispose();
        }
    }
}
