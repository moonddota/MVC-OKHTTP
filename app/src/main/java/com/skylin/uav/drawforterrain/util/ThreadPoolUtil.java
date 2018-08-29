package com.skylin.uav.drawforterrain.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/5/24.
 */

public class ThreadPoolUtil {
    public static void thread(Runnable runnable){
        /**
         * 创建可以容纳3个线程的线程池
         * **/
//        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        /**
         *  线程池的大小会根据执行的任务数动态分配
         * **/
        ExecutorService threadPool = Executors.newCachedThreadPool();
        /**
         *  创建单个线程的线程池，如果当前线程在执行任务时突然中断，则会创建一个新的线程替代它继续执行任务
         * **/
//        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        /**
         *  创建一个可安排在给定延迟后运行命令或者定期地执行的线程池 效果类似于Timer定时器
         * **/
//        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(3);

        threadPool.execute(runnable);//开始
        threadPool.shutdown();//关闭
    }

    /**
     * 销毁线程方法
     */
    public static void destroyThread( Thread thread) {
        try {
            if (null != thread && Thread.State.RUNNABLE == thread .getState()) {
                try {
                    Thread.sleep(500);
                    thread .interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            thread = null;
        }
    }
}
