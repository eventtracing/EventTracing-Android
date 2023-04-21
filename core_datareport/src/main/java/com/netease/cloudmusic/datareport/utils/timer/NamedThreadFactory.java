package com.netease.cloudmusic.datareport.utils.timer;

import androidx.annotation.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final ThreadFactory mDefaultThreadFactory;
    private final String mBaseName;
    private final AtomicInteger mCount = new AtomicInteger(0);

    public NamedThreadFactory(final String baseName) {
        mDefaultThreadFactory = Executors.defaultThreadFactory();
        mBaseName = baseName;
    }

    @Override
    public Thread newThread(@NonNull final Runnable runnable) {
        final Thread thread = mDefaultThreadFactory.newThread(runnable);
        thread.setName(mBaseName + "-" + mCount.getAndIncrement());
        return thread;
    }
}
