package com.netease.cloudmusic.datareport.app;

import android.content.SharedPreferences;

import com.netease.cloudmusic.datareport.provider.ProcessPreferences;
import com.netease.cloudmusic.datareport.utils.ReportUtils;

import java.util.ArrayList;

public class AppGroundStateStorage {
    private static final String NAME = "com.netease.cluodmusic.datareport.appground.storage";

    static final String FOREGROUND_ACTIVITY_VISUAL = "FOREGROUND_ACTIVITY_VISUAL";
    static final String FOREGROUND_ACTIVITY_RESUME_PID = "FOREGROUND_ACTIVITY_RESUME_PID";
    static final String FOREGROUND_ACTIVITY_PAUSE = "FOREGROUND_ACTIVITY_PAUSE";

    private static ProcessPreferences getDefaultStorage() {
        return ProcessPreferences.Companion.getInstance(ReportUtils.getContext(), NAME, ProcessPreferences.MODE_IN_MEMORY);
    }

    static void setActivityVisual(boolean activityVisual) {
        getDefaultStorage().edit().putBoolean(FOREGROUND_ACTIVITY_VISUAL, activityVisual).apply();
    }

    static boolean isActivityVisual() {
        return getDefaultStorage().getBoolean(FOREGROUND_ACTIVITY_VISUAL, false);
    }

    static void setActivityPause(boolean pause) {
        getDefaultStorage().edit().putBoolean(FOREGROUND_ACTIVITY_PAUSE, pause).apply();
    }

    /**
     * 记录最近resume的activity所在的pid
     *
     * @param pid
     */
    static void setActivityResumePid(int pid) {
        getDefaultStorage().edit().putInt(FOREGROUND_ACTIVITY_RESUME_PID, pid).apply();
    }

    /**
     * 获取最近resume的activity所在的pid
     *
     * @return
     */
    static int getActivityResumePid() {
        return getDefaultStorage().getInt(FOREGROUND_ACTIVITY_RESUME_PID, -1024);
    }

    static boolean isActivityPause() {
        return getDefaultStorage().getBoolean(FOREGROUND_ACTIVITY_PAUSE, false);
    }

    static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener mVisibleListener, ArrayList<String> keys) {
        getDefaultStorage().registerOnSharedPreferenceChangeListener(mVisibleListener, keys);
    }
}
