package com.netease.cloudmusic.datareport.utils.timer;

import android.os.SystemClock;

import androidx.annotation.VisibleForTesting;

import com.netease.cloudmusic.datareport.app.AppEventReporter;
import com.netease.cloudmusic.datareport.event.AppEventType;
import com.netease.cloudmusic.datareport.event.EventDispatch;
import com.netease.cloudmusic.datareport.event.EventKey;
import com.netease.cloudmusic.datareport.event.IEventType;
import com.netease.cloudmusic.datareport.provider.ProcessPreferences;
import com.netease.cloudmusic.datareport.report.InnerReportKeyKt;
import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.utils.ReportUtils;

import java.util.HashMap;
import java.util.Map;
/**
 * App时长统计接口。计时规则如下：
 * <p>
 * 1. appin时间戳记做  t1; 时长参数为duration
 * <p>
 * 2. appin时延迟5s开始计时器，计时器心跳位5s
 * <p>
 * 3. 计时器产生一次心跳，若心跳时间戳位 t2, 则时间差interval = t2 - t1, 如果interval > 10s，则将duration+= 5;如果interval <= 10s, 则duration+= interval。每一次心跳更新t1 为t2。
 * <p>
 * 4. appout时间戳为t3, duration累加规则与心跳一致。
 */
public class StorageDurationTimer implements IDurationTimer {

    private ProcessPreferences sp;

    public StorageDurationTimer() {
        sp = ProcessPreferences.Companion.getInstance(ReportUtils.getContext(), TIMER_STORAGE_NAME);
    }

    private static final String TIMER_STORAGE_NAME = "timer_storage_name";

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
        updateAppHeartTime();
        mLatestTs = getCurrentTs();
    }

    private void updateAppHeartTime() {
        sp.edit().putLong(AppEventReporter.APP_HEART_TIME, mDuration).apply();
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
        updateAppHeartTime();
    }

    @VisibleForTesting
    protected long getCurrentTs() {
        return SystemClock.uptimeMillis();
    }

    private void reportAO(long durationTime) {
        Map<String, Object> params = new HashMap<>();
        params.put(InnerReportKeyKt.REPORT_KEY_LVTM_HEART, durationTime);
        params.put(InnerReportKeyKt.REPORT_KEY_LVTM, durationTime);

        IEventType eventType = new AppEventType(EventKey.APP_OUT, params);
        EventDispatch.INSTANCE.onEventNotifier(eventType, null);
    }
}
