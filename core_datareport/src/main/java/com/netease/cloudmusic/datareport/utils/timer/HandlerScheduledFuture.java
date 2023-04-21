package com.netease.cloudmusic.datareport.utils.timer;

import android.os.Handler;
import android.os.SystemClock;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HandlerScheduledFuture<T> extends FutureTask<T> implements RunnableScheduledFuture<T> {

    private final long mPeriodic;
    private Handler mHandler;
    private long mDelay;
    private int count;
    private long mInit;

    HandlerScheduledFuture(Runnable runnable, Handler handler,
                           long delay, long periodic) {
        super(runnable, null);

        mHandler = handler;
        mDelay = delay;
        mPeriodic = periodic;
        mInit = SystemClock.uptimeMillis();
    }

    HandlerScheduledFuture(Callable<T> callable, Handler handler,
                           long delay, long periodic) {
        super(callable);

        mHandler = handler;
        mDelay = delay;
        mPeriodic = periodic;
        mInit = SystemClock.uptimeMillis();
    }

    @Override
    public boolean isPeriodic() {
        return mPeriodic > 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return mDelay;
    }

    @Override
    public int compareTo(Delayed o) {
        return (int) (mDelay - o.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public void run() {
        boolean periodic = isPeriodic();
        boolean canceled = isCancelled();
        if (canceled) {
            cancel(false);
        } else if (!periodic) {
            super.run();
            count++;
        } else {
            reExecutePeriodic(this, setNextRunTime());
        }
    }

    private void reExecutePeriodic(Runnable runnable, long next) {
        mHandler.postAtTime(this, next);
    }

    long setNextRunTime() {
        return mInit + mDelay + count * mPeriodic;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        mHandler.removeCallbacks(this);
        return true;
    }
}
