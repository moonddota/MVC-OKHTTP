package com.skylin.mavlink.utils;


import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import sjj.alog.Log;

/**
 * Created by sjj on 2017/9/8.
 */
public final class Pool {
    private Pool() {
    }

    public static Disposable submit(@NonNull Scheduler scheduler, @NonNull final Runnable runnable, @NonNull final Consumer<Throwable> consumer) {
        return scheduler.createWorker().schedule(new Runnable() {
            @Override
            public void run() {

                try {
                    runnable.run();
                } catch (Exception e) {
                    try {
                        consumer.accept(e);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    public static Disposable submit(@NonNull Runnable runnable, @NonNull Consumer<Throwable> consumer) {
        return submit(Schedulers.computation(), runnable, consumer);
    }

    public static Disposable submit(@NonNull Scheduler scheduler, @NonNull final Runnable runnable) {
        return submit(scheduler, runnable, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e("pool error", throwable);
            }
        });
    }

    public static Disposable submit(@NonNull final Runnable runnable) {
        return submit(Schedulers.computation(), runnable);
    }

    public static Disposable submit(@NonNull Scheduler scheduler, @NonNull final Runnable run, long delay, @NonNull final Consumer<Throwable> consumer) {
        return scheduler.createWorker().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    run.run();
                } catch (Exception e) {
                    try {
                        consumer.accept(e);
                    } catch (Exception ignored) {
                    }
                }
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public static Disposable submit(@NonNull Runnable runnable, long delay, @NonNull Consumer<Throwable> consumer) {
        return submit(Schedulers.computation(), runnable, delay, consumer);
    }

    public static Disposable submit(@NonNull Scheduler scheduler, @NonNull final Runnable runnable, long delay) {
        return submit(scheduler, runnable, delay, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e("pool error", throwable);
            }
        });
    }

    public static Disposable submit(@NonNull final Runnable runnable, long delay) {
        return submit(Schedulers.computation(), runnable, delay);
    }

    public static Disposable submit(@NonNull Scheduler scheduler, @NonNull final Runnable run, final long initialDelay, final long period, @NonNull final Consumer<Throwable> consumer) {
        return scheduler.createWorker().schedulePeriodically(new Runnable() {
            @Override
            public void run() {
                try {
                    run.run();
                } catch (Exception e) {
                    try {
                        consumer.accept(e);
                    } catch (Exception ignored) {
                    }
                }
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public static Disposable submit(@NonNull Runnable runnable, final long initialDelay, final long period, @NonNull Consumer<Throwable> consumer) {
        return submit(Schedulers.computation(), runnable, initialDelay, period, consumer);
    }

    public static Disposable submit(@NonNull Scheduler scheduler, @NonNull final Runnable runnable, final long initialDelay, final long period) {
        return submit(scheduler, runnable, initialDelay, period, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e("pool error", throwable);
            }
        });
    }

    public static Disposable submit(@NonNull final Runnable runnable, final long initialDelay, final long period) {
        return submit(Schedulers.computation(), runnable, initialDelay, period);
    }

}

