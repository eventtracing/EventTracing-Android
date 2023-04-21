package com.netease.cloudmusic.datareport.utils.timer;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.*;

public class ScheduledHandlerExecutorService extends AbstractExecutorService
        implements ScheduledExecutorService {

    private Handler mHandler;

    ScheduledHandlerExecutorService(Looper looper) {
        mHandler = new Handler(looper);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        HandlerScheduledFuture<?> future = new HandlerScheduledFuture<Object>(
                command, mHandler, unit.convert(delay, TimeUnit.MILLISECONDS), 0);
        mHandler.postAtTime(future, future.setNextRunTime());
        return future;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        HandlerScheduledFuture<V> future = new HandlerScheduledFuture<V>(
                callable, mHandler, unit.convert(delay, TimeUnit.MILLISECONDS), 0);
        mHandler.postAtTime(future, future.setNextRunTime());
        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,
                                                  long period, TimeUnit unit) {
        if (period > 0) {
            HandlerScheduledFuture<?> future = new HandlerScheduledFuture<Object>(
                    command, mHandler, unit.convert(initialDelay, TimeUnit.MILLISECONDS),
                    unit.convert(period, TimeUnit.MILLISECONDS));
            mHandler.postAtTime(future, future.setNextRunTime());
            return future;
        } else {
            return schedule(command, initialDelay, unit);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
                                                     long delay, TimeUnit unit) {
        if (delay > 0) {
            HandlerScheduledFuture<?> future = new HandlerScheduledFuture<Object>(
                    command, mHandler, unit.convert(initialDelay, TimeUnit.MILLISECONDS),
                    unit.convert(delay, TimeUnit.MILLISECONDS));
            mHandler.postAtTime(future, future.setNextRunTime());
            return future;
        } else {
            return schedule(command, initialDelay, unit);
        }
    }

    @Override
    public void shutdown() {
        mHandler.removeCallbacks(null);
    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        mHandler.post(command);
    }
}
