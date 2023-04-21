package com.netease.cloudmusic.datareport.utils;

import com.netease.cloudmusic.datareport.provider.ILogger;
import com.netease.cloudmusic.datareport.inner.DataReportInner;

/**
 * 日志类 所有日志输出请通过此类进行
 */
public class Log {

    public static void debug(String tag, String msg) {
        ILogger logger = getLogger();
        if (DataReportInner.getInstance().isDebugMode() && logger != null) {
            logger.debug(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        ILogger logger = getLogger();
        if (DataReportInner.getInstance().isDebugMode() && logger != null) {
            logger.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        ILogger logger = getLogger();
        if (DataReportInner.getInstance().isDebugMode() && logger != null) {
            logger.d(tag, msg);
        }
    }

    public static void ddf(String tag, String format, Object... args) {
        String result = format;
        try {
            result = String.format(format, args);
        } catch (Exception ignore) {
        }
        d(tag, result);
    }

    public static void i(String tag, String msg) {
        ILogger logger = getLogger();
        if (DataReportInner.getInstance().isDebugMode() && logger != null) {
            logger.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        ILogger logger = getLogger();
        if (DataReportInner.getInstance().isDebugMode() && logger != null) {
            logger.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        ILogger logger = getLogger();
        if (DataReportInner.getInstance().isDebugMode() && logger != null) {
            logger.e(tag, msg);
        }
    }

    private static ILogger getLogger() {
        return DataReportInner.getInstance().getConfiguration().getLogger();
    }
}
