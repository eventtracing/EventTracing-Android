package com.netease.cloudmusic.datareport.inner;

import android.util.Log;

import com.netease.cloudmusic.datareport.provider.ILogger;

/**
 * 默认Logger实现，输出至logcat
 */
public class DefaultLogger implements ILogger {

    private DefaultLogger() {
    }

    private static final DefaultLogger INSTANCE = new DefaultLogger();

    public static DefaultLogger getInstance() {
        return INSTANCE;
    }

    @Override
    public void debug(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        Log.e(tag, msg);
    }
}
