package com.netease.cloudmusic.datareport.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * 线程工具类
 */
public class ThreadUtils {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(Runnable r) {
        if (r != null) {
            HANDLER.post(r);
        }
    }
}
