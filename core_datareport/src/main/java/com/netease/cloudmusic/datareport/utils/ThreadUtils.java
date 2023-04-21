package com.netease.cloudmusic.datareport.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工具类
 */
public class ThreadUtils {

    private static final AtomicInteger NUMBER = new AtomicInteger(1);

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "vrpool-" + NUMBER.getAndIncrement() + "-thread");
        }
    });

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static void execTask(Runnable r) {
        if (r != null) {
            EXECUTOR.execute(r);
        }
    }

    public static void execTask(Runnable r, boolean isMainThread) {
        if (r == null) {
            return;
        }
        if (isMainThread) {
            r.run();
        } else {
            execTask(r);
        }
    }

    public static void runOnUiThread(Runnable r) {
        if (r != null) {
            HANDLER.post(r);
        }
    }
}
