package com.netease.cloudmusic.datareport.utils.timer;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerTaskManager {

    // 内部类 用于线程安全和延迟加载 利用java自身机制
    private static class TimerTaskManagerHolder {
        private static TimerTaskManager mInstence = new TimerTaskManager();
    }

    private static final String TAG = "TimerTaskManager";

    private final ConcurrentHashMap<String, Future<?>> mWorkingGroup = new ConcurrentHashMap<String, Future<?>>();

    private final static String mIDPrefix = "TimerTask_ID_";
    private final AtomicInteger nextID = new AtomicInteger(0);

    private final ScheduledExecutorService mThreadExecutor;

    private TimerTaskManager() {
        mThreadExecutor = new ScheduledThreadPoolExecutor(
                4, new NamedThreadFactory(TAG), new ThreadPoolExecutor.AbortPolicy());
    }

    public static TimerTaskManager getInstance() {
        return TimerTaskManagerHolder.mInstence;
    }

    //带leakwatcher的版本对于静态内部类这种类型可能有问题，只能作为debug下使用
    public String addTimerTask(Runnable task, long delay, long period) {

        if (task == null) {
            throw new NullPointerException("runnable is null");
        }

        String key = mIDPrefix + nextID.incrementAndGet();

        task = new WatcherRunnable(task, key, period > 0);

        ScheduledFuture<?> future;
        if (period > 0) {
            future = mThreadExecutor.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
        } else {
            future = mThreadExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
        }

        mWorkingGroup.put(key, future);
        return key;
    }

    public void cancelTimerTask(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        Future<?> future = mWorkingGroup.remove(key);
        if (future != null) {
            future.cancel(true);
        }
    }

    private class WatcherRunnable implements Runnable {

        private Runnable mRunnable;
        private String mKey;
        private boolean mIsPeriod;

        WatcherRunnable(Runnable runnable, String key, boolean isPeriod) {
            mRunnable = runnable;
            mKey = key;
            mIsPeriod = isPeriod;
        }

        @Override
        public void run() {
            try {
                mRunnable.run();
            } finally {
                if (!mIsPeriod) {
                    mWorkingGroup.remove(mKey);
                }
            }
        }
    }
}
