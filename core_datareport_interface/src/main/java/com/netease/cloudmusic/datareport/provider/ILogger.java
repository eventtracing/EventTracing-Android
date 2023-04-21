package com.netease.cloudmusic.datareport.provider;

/**
 * 定义一个日志输出接口
 */
public interface ILogger {

    void debug(String tag, String msg);

    void v(String tag, String msg);

    void d(String tag, String msg);

    void i(String tag, String msg);

    void w(String tag, String msg);

    void e(String tag, String msg);
}
