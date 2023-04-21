package com.netease.cloudmusic.datareport.utils.timer;

import android.os.SystemClock;

import androidx.annotation.VisibleForTesting;

import com.netease.cloudmusic.datareport.utils.Log;

public class DurationTimer implements IDurationTimer {

    private static final String TAG = "DurationTimer";

    /**
     * 心跳间隔
     */
    private static final int TIMER_INTERVAL = 5_000;

    /**
     * 允许时钟的最大间隔
     */
    private static final int THRESHOLD = 10_000;

    /**
     * App停留时长
     */
    private long mDuration;

    /**
     * 最近打点的时间戳，ms级别
     */
    private long mLatestTs;

    /**
     * 心跳任务句柄
     */
    private String mBeatTimerKey;

    @Override
    public void startTimer() {
        Log.i(TAG, "startTimer");
        reset();
        mBeatTimerKey = TimerTaskManager.getInstance().addTimerTask(new Runnable() {
            @Override
            public void run() {
                updateDuration();
            }
        }, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    @Override
    public void stopTimer() {
        updateDuration();
        if (mBeatTimerKey != null) {
            TimerTaskManager.getInstance().cancelTimerTask(mBeatTimerKey);
            Log.i(TAG, "stopTimer");
        }
    }

    @Override
    public long getDuration() {
        long duration = mDuration;
        Log.i(TAG, "getDuration = " + duration);
        reset();
        return duration;
    }

    @Override
    public void reset() {
        mDuration = 0;
        mLatestTs = getCurrentTs();
    }

    /**
     * 心跳或appout打点。
     */
    private void updateDuration() {
        updateDuration(getCurrentTs());
    }

    @VisibleForTesting
    final void updateDuration(long curTs) {
        long interval = curTs - mLatestTs;
        if (interval > THRESHOLD) {
            interval = TIMER_INTERVAL;
            Log.i(TAG, "心跳间隔异常 = " + interval);
        }
        Log.i(TAG, "update, interval = " + interval);
        mDuration += interval;
        // 更新打点事件
        mLatestTs = curTs;
    }

    @VisibleForTesting
    protected long getCurrentTs() {
        return SystemClock.uptimeMillis();
    }
}
