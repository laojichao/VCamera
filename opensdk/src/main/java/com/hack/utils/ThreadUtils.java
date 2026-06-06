package com.hack.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程操作工具类。
 * <p>
 * 提供主线程判断、主线程任务提交、后台线程池任务提交等常用线程操作。
 * 后台线程池采用双重检查锁定的懒加载方式初始化，核心线程数取
 * {@link #ASYNC_MAX_THREAD} 与 CPU 核心数的较小值，空闲线程超过 5 秒自动回收。
 * </p>
 */
public class ThreadUtils {
    private static Thread sMainThread;
    private static Handler sMainThreadHandler;
    private static volatile ThreadPoolExecutor sThreadExecutor;

    /** 后台线程池最大核心线程数 */
    public static final int ASYNC_MAX_THREAD = 4;

    static {
        sMainThread = Looper.getMainLooper().getThread();
        sMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 判断当前线程是否为主线程（UI 线程）。
     *
     * @return {@code true} 表示当前线程为主线程
     */
    public static boolean isMainThread() {
        return Thread.currentThread() == sMainThread;
    }

    /**
     * 获取主线程的 Handler。
     *
     * @return 主线程 {@link Handler} 实例
     */
    public static Handler getUiThreadHandler() {
        return sMainThreadHandler;
    }

    /**
     * 校验当前线程是否为主线程。
     * <p>若当前线程不是主线程，则抛出 {@link RuntimeException}。</p>
     *
     * @throws RuntimeException 若当前线程不是主线程
     */
    public static void ensureMainThread() {
        if (!isMainThread()) {
            throw new RuntimeException("Must be called on the UI thread");
        }
    }

    /**
     * 在后台线程池中提交 Runnable 任务。
     *
     * @param runnable 待执行的 Runnable 任务
     * @return {@link Future} 对象，可用于监控任务状态或取消任务
     */
    public static Future postOnBackgroundThread(Runnable runnable) {
        return getThreadExecutor().submit(runnable);
    }

    /**
     * 在后台线程池中提交 Callable 任务。
     *
     * @param callable 待执行的 Callable 任务
     * @return {@link Future} 对象，可用于获取任务结果、监控状态或取消任务
     */
    public static Future postOnBackgroundThread(Callable callable) {
        return getThreadExecutor().submit(callable);
    }

    /**
     * 在主线程上提交 Runnable 任务。
     * <p>通过主线程 Handler 将任务投递到主线程消息队列中执行。</p>
     *
     * @param runnable 待在主线程执行的 Runnable 任务
     */
    public static void postOnMainThread(Runnable runnable) {
        getUiThreadHandler().post(runnable);
    }

    /**
     * 获取后台线程池执行器。
     * <p>采用双重检查锁定（DCL）模式懒加载初始化。核心线程数取 CPU 核心数与
     * {@link #ASYNC_MAX_THREAD} 的较小值，空闲线程 5 秒后自动回收。</p>
     *
     * @return 后台线程池 {@link ExecutorService} 实例
     */
    private static ExecutorService getThreadExecutor() {
        if (sThreadExecutor == null) {
            synchronized (ThreadUtils.class) {
                if (sThreadExecutor == null) {
                    int coreNum = Runtime.getRuntime().availableProcessors();
                    if (coreNum > ASYNC_MAX_THREAD) coreNum = ASYNC_MAX_THREAD;

                    sThreadExecutor = new ThreadPoolExecutor(coreNum, coreNum,
                            5L, TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>());
                    sThreadExecutor.allowCoreThreadTimeOut(true);
                }
            }
        }
        return sThreadExecutor;
    }
}
