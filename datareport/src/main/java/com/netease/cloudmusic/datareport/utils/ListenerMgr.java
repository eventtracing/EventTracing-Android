package com.netease.cloudmusic.datareport.utils;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 监听者模式管理器。用于管理多监听者，和批量回调。注册的监听者是以弱引用的方式存在的。
 */
public class ListenerMgr<T> {

    public interface INotifyCallback<T> {
        void onNotify(T listener);
    }

    private final ConcurrentLinkedQueue<WeakReference<T>> mListenerQueue = new ConcurrentLinkedQueue<WeakReference<T>>();

    public void register(T listener) {
        if (listener == null) {
            return;
        }

        synchronized (mListenerQueue) {
            boolean contain = false;
            for (Iterator<WeakReference<T>> iterator = mListenerQueue.iterator(); iterator.hasNext(); ) {
                T listenerItem = iterator.next().get();
                if (listenerItem == null) {
                    iterator.remove();
                } else if (listenerItem == listener) {
                    contain = true;
                }
            }

            if (!contain) {
                WeakReference<T> weakListener = new WeakReference<>(listener);
                mListenerQueue.add(weakListener);
            }
        }
    }

    public void unregister(T listener) {
        if (listener == null) {
            return;
        }

        synchronized (mListenerQueue) {
            for (Iterator<WeakReference<T>> iterator = mListenerQueue.iterator(); iterator.hasNext(); ) {
                T listenerItem = iterator.next().get();
                if (listenerItem == listener) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    public void startNotify(INotifyCallback<T> callback) {
        ConcurrentLinkedQueue<WeakReference<T>> copyListenerQueue = null;
        synchronized (mListenerQueue) {
            if (mListenerQueue.size() > 0) {
                copyListenerQueue = new ConcurrentLinkedQueue<WeakReference<T>>(mListenerQueue);
            }
        }

        if (copyListenerQueue != null) {
            try {
                for (WeakReference<T> aCopyListenerQueue : copyListenerQueue) {
                    T listenerItem = aCopyListenerQueue.get();
                    if (listenerItem != null) {
                        try {
                            callback.onNotify(listenerItem);
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        synchronized (mListenerQueue) {
            mListenerQueue.clear();
        }
    }

    public int size() {
        synchronized (mListenerQueue) {
            return mListenerQueue.size();
        }
    }
}