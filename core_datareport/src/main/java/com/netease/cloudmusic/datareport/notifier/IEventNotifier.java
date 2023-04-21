package com.netease.cloudmusic.datareport.notifier;

/**
 * 事件通知者
 */
public interface IEventNotifier {
    int getReuseType();

    void notifyEvent(IEventListener listener);

    void reset();
}
