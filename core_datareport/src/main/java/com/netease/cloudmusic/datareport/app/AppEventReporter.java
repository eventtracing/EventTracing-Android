package com.netease.cloudmusic.datareport.app;

import android.app.Activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.netease.cloudmusic.datareport.data.DataRWProxy;
import com.netease.cloudmusic.datareport.event.AppEventType;
import com.netease.cloudmusic.datareport.event.EventDispatch;
import com.netease.cloudmusic.datareport.event.IEventType;
import com.netease.cloudmusic.datareport.inner.InnerKey;
import com.netease.cloudmusic.datareport.provider.ProcessPreferences;
import com.netease.cloudmusic.datareport.report.InnerReportKeyKt;
import com.netease.cloudmusic.datareport.event.EventKey;

import com.netease.cloudmusic.datareport.inject.EventCollector;
import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener;

import com.netease.cloudmusic.datareport.report.data.PageStepManager;
import com.netease.cloudmusic.datareport.report.refer.ReferManager;
import com.netease.cloudmusic.datareport.utils.ListenerMgr;
import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.utils.ProcessUtils;
import com.netease.cloudmusic.datareport.utils.ReportUtils;

import com.netease.cloudmusic.datareport.R;
import com.netease.cloudmusic.datareport.utils.UIUtils;
import com.netease.cloudmusic.datareport.utils.timer.StorageDurationTimer;
import com.netease.cloudmusic.datareport.vtree.VTreeManager;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 根据APP的状态来上报，有激活，启动，前台，后台
 */
public class AppEventReporter extends DefaultEventListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PROCESS_PREFERENCE_APP_DEFAULT_NAME = "com.netease.cloudmusic.datareport.app.default"; //保存当前的所有跨进程的数据的name
    public static final String PREFERENCE_SESSION_KEY = "session_id"; //保存sessionid的key
    public static final String LAST_SESSION_ID = "last_session_id"; //上一次的sessionid
    public static final String APP_IN_TIME_KEY = "app_in_time_key";//这一次app进入前台的时间

    public static final String APP_HEART_TIME = "app_heard_time";//心跳时间记录在sp里面跨进错

    private static final String CURRENT_PROCESS_ACTIVITY_NUM_KEY = "current_process_activity_num";//用来存储当前启动的activity数量，当进程启动的时候这个值被清零，要在后面拼上进程名称

    private static final String TAG = "AppEventReporter";

    private static final String BACKGROUND_BROADCAST_ACTION = ".datareport.app.background.report"; //进入后台的广播

    private static String getBackgroundBroadcastAction() {
        return ReportUtils.getContext().getPackageName() + BACKGROUND_BROADCAST_ACTION;
    }

    private int mActivityCount = 0; //针对与当前进程的activity的数量，存在内存中
    private boolean mAppForegroundReported = false; //针对当前进程，是否在前台
    private String mUsId = ""; //每个进程在内存保存的sessionid
    private String lastSessionId = ""; //每个进程在内存保存的上一次sessionid
    private final ListenerMgr<IAppEventListener> mListenerMgr = new ListenerMgr<>();
    private final HashSet<Integer> mActivityHashCode = new HashSet<>();

    private ProcessPreferences defaultPreferences;

    private boolean isCurrentProcessColdStart = true;//只在本进程的维度来判断当前这个进程是否是冷启动

    private Activity currentActivity = null; //当前的activity

    private final List<WeakReference<Activity>> activityStack = new ArrayList<>();

    //===========多进程前后台切换===========
    private static final int CHECK_DELAY = 10000;//增加前后台切换的delay判断，性能较差的机型上需要时间长一点,兜底10秒
    private final Handler mHandler = new Handler();
    private Runnable mAppGroundCheck;
    private final ApplicationObserver mApplicationObserver = new ApplicationObserver();

    private final StorageDurationTimer timer = new StorageDurationTimer();

    private void initGround() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(mApplicationObserver);
    }
    private void activityResume(Activity activity) {
        setActivityPause(false);
        boolean wasBackground = isBackground();
        setActivityVisual(true);
        if (mAppGroundCheck != null) {
            mHandler.removeCallbacks(mAppGroundCheck);
            mAppGroundCheck = null;
        }
        currentActivity = activity;
        if (wasBackground) {
            onAppForeground();
        }
    }

    private void activityPaused(Activity activity) {
        setActivityPause(true);
        checkBackground(CHECK_DELAY); //onActivityStop有可能会很久之后才回调（6%左右的手机在2秒后）；因此onPause后，3秒后检查是否在后台
    }

    private void activityStop(Activity activity){
        if (currentActivity == activity) {
            currentActivity = null;
        }
        //多进程情况下面本进程stop会可能早于另外进程的onresume，所以改成始终延迟1s，但有可能1s启动另外一个进程onresume来不及，可能会造成先到后台再回前台的回调
        checkBackground(1000);
    }

    private void checkBackground(final long delay) {
        if (mAppGroundCheck != null) {
            mHandler.removeCallbacks(mAppGroundCheck);
            mAppGroundCheck = null;
        }
        mHandler.postDelayed(mAppGroundCheck = () -> {
            // 在原先的前后台判断存在一个bug，当透明主题activity存在的时候 透明主题activity第二次启动 一个activity的时候，
            // 透明主题底下的的activity 生命周期会自己走一下onResume 和 onPause 导致isActivityPause() 判断为true 回调了onAppBackground
            // 现在修改加入一个并且的条件，当前进程是否进入后台
            if (isActivityVisual() && isActivityPause() && mApplicationObserver.isCurrentProcessIsBackground()) {
                setActivityVisual(false);
                //适配多进程通过广播来做
                ReportUtils.getContext().sendBroadcast(new Intent(getBackgroundBroadcastAction()));
            }
            mAppGroundCheck = null;
        }, delay);
    }

    public boolean isForeground() {
        return isActivityVisual();
    }

    public boolean isBackground() {
        return !isForeground();
    }

    private boolean isActivityPause() {
        return AppGroundStateStorage.isActivityPause();
    }
    private void setActivityPause(boolean pause) {
        final int curPid = android.os.Process.myPid();
        if (pause) {
            //当activity pasue的时候，检查上一个resume的activity所处的进程id，是否与当前进程的pid相同
            // 如果相同则可以安心的置为pause；不然说明有其他进程的activity在前面，则不置为pause
            final int lastResumePid = AppGroundStateStorage.getActivityResumePid();
            if (lastResumePid == curPid) {
                AppGroundStateStorage.setActivityPause(true);
            }
        } else {
            AppGroundStateStorage.setActivityResumePid(curPid);
            AppGroundStateStorage.setActivityPause(false);
        }
    }

    private void setActivityVisual(boolean activityVisual) {
        AppGroundStateStorage.setActivityVisual(activityVisual);
    }

    public boolean isActivityVisual() {
        return AppGroundStateStorage.isActivityVisual();
    }

    /**
     * 观察者，观察当前进程前后台状态
     */
    private class ApplicationObserver implements LifecycleObserver {

        private boolean mCurrentProcessIsBackground = true;

        /**
         * 应用程序出现到前台时调用
         */
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onStart() {
            mCurrentProcessIsBackground = false;
        }

        /**
         * 应用程序出现到前台时调用
         */
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void onResume() {
            mCurrentProcessIsBackground = false;
        }

        /**
         * 应用程序退出到后台时调用
         */
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void onPause() {
            mCurrentProcessIsBackground = true;
            checkBackground(CHECK_DELAY);
        }

        /**
         * 应用程序退出到后台时调用
         */
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop() {
            //不做是否已经进入后台的判断 不然可能不会重置onPause里面的回调，导致回调会在CHECK_DELAY延迟后才会回调 进入后台接口
            mCurrentProcessIsBackground = true;
            checkBackground(500);
        }

        /**
         * 是否在后台
         * @return 在后台
         */
        public boolean isCurrentProcessIsBackground() {
            return mCurrentProcessIsBackground;
        }
    }

    //===========多进程前后台切换===========

    /**
     * APP状态监听器
     */
    public interface IAppEventListener {

        /**
         * APP进后台
         *
         * @param isMainThread 是否在主线程中进行AppOut
         */
        void onAppOut(boolean isMainThread);

        /**
         * APP进前台
         */
        void onAppIn();
    }

    /**
     * 注册APP状态监听
     */
    public void register(IAppEventListener listener) {
        mListenerMgr.register(listener);
    }

    /**
     * 反注册APP状态监听
     */
    public void unregister(IAppEventListener listener) {
        mListenerMgr.unregister(listener);
    }

    public static AppEventReporter getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private AppEventReporter() {
    }

    /**
     * 进程启动的时候在application的时候进行初始化
     */
    private boolean isComputeActivityNum = false;
    private void init() {
        defaultPreferences = ProcessPreferences.Companion.getInstance(ReportUtils.getContext(), PROCESS_PREFERENCE_APP_DEFAULT_NAME);
        //直接获取sessionid
        mUsId = defaultPreferences.getString(PREFERENCE_SESSION_KEY, "");
        //清除该进程上一次启动的时候保留的activity的数量
        clearCurrentProcessActivityNum();
        ArrayList<String> list = new ArrayList<>();
        list.add(PREFERENCE_SESSION_KEY);
        //监听sessionid发生变化的情况
        defaultPreferences.registerOnSharedPreferenceChangeListener(this, list);
        EventCollector.getInstance().registerEventListener(this);
        initGround();
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREFERENCE_SESSION_KEY.equals(key)) {
            //得到消息，sessionid发生变化了，需要重置sessionid和lastSessionId
            String tempUsId = sharedPreferences.getString(key, "");
            if (!mUsId.equals(tempUsId)) {
                lastSessionId = mUsId;
                mUsId = tempUsId;
            }
        }
    }

    private static class InstanceHolder {
        private static final AppEventReporter INSTANCE;

        static {
            INSTANCE = new AppEventReporter();
            INSTANCE.init();
        }
    }

    @Override
    public void onActivityCreate(Activity activity) {
        appStartDataSender(activity); // app冷启动的判断与处理
        super.onActivityCreate(activity);
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "onActivityCreate: activity=" + activity);
        }
        activityStack.add(new WeakReference<>(activity));
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityStarted: activity=" + activity);
        }
        mActivityCount++;
        //更新本进程的activity数量到contentprovider，供其他进程查询
        updateCurrentProcessActivityNum();
        mActivityHashCode.add(activity.hashCode());
    }

    @Override
    public void onActivityResume(Activity activity) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityResume: activity=" + activity);
        }
        // appStart和appIn放在Resume中
        appInDataSender(); //app是否到前台的判断与处理
        activityResume(activity);
    }

    @Override
    public void onActivityPause(Activity activity) {
        super.onActivityPause(activity);
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "onActivityPause: activity=" + activity);
        }
        activityPaused(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityStop(activity);
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityStopped: activity=" + activity);
        }
        if (!mActivityHashCode.remove(activity.hashCode())) {
            String msg = activity.getApplicationContext().getString(R.string.lifecycle_not_matched, activity.toString());
            if (DataReportInner.getInstance().isDebugMode()) {
                Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
            Log.e(TAG, msg);
            return;
        }
        //多进程同步的activity数量和本地的activity数量都减一
        mActivityCount--;
        updateCurrentProcessActivityNum();

        if (mActivityCount <= 0) { //如果当前进程的activity数量为0的话，判断app是否到后台以及处理
            appOutDataSender(false);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = activityStack.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            if (reference.get() == activity) {
                iterator.remove();
            }
        }
        super.onActivityDestroyed(activity);
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "onActivityDestroyed: activity=" + activity);
        }
    }

    /**
     * app启动上报
     */
    private void appStartDataSender(Activity activity) {
        if (isCurrentProcessColdStart) {
            isCurrentProcessColdStart = false;
            if (isColdAppStart(activity)) {
                lastSessionId = mUsId;
                mUsId = buildSessionId();
                defaultPreferences.edit().putString(PREFERENCE_SESSION_KEY, mUsId).putString(LAST_SESSION_ID, lastSessionId).apply();
                ReferManager.INSTANCE.clearData();//清除上次启动遗留的数据
                VTreeManager.INSTANCE.clear();
                PageStepManager.INSTANCE.clear();//清除上次启动遗留的数据
                appStartDataSenderInner();
            } else {
                lastSessionId = defaultPreferences.getString(LAST_SESSION_ID, "");
            }
        }
    }

    /**
     * 更新本进程的activity数量到contentprovider，供其他进程查询
     */
    private void updateCurrentProcessActivityNum() {
        Context context = ReportUtils.getContext();
        if (context != null) {
            defaultPreferences.edit().putInt(CURRENT_PROCESS_ACTIVITY_NUM_KEY + ProcessUtils.getCurrentProcessName(), mActivityCount).apply();
        }
    }

    private void clearCurrentProcessActivityNum() {
        Context context = ReportUtils.getContext();
        if (context != null) {
            defaultPreferences.edit().putInt(CURRENT_PROCESS_ACTIVITY_NUM_KEY + ProcessUtils.getCurrentProcessName(), 0).apply();
        }
    }

    private int getProcessActivityNum(String processName) {
        return defaultPreferences.getInt(CURRENT_PROCESS_ACTIVITY_NUM_KEY + processName, 0);
    }

    private boolean isColdAppStart(Activity activity) {
        Context context = ReportUtils.getContext();
        if (context != null) {
            ActivityManager am = (ActivityManager) ReportUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        List<ActivityManager.AppTask> tasks = am.getAppTasks();
                        if (tasks.size() > 1) {
                            return false;
                        } else if (tasks.size() == 1) {
                            ActivityManager.RecentTaskInfo info = tasks.get(0).getTaskInfo();
                            if (activity.getComponentName().equals(info.baseActivity)) {
                                return true;
                            }
                        } else {
                            return false;
                        }
                    } catch (SecurityException exception) {
                        exception.printStackTrace();
                    }
                }
            }

            //由于禁止使用getRunningAppProcesses方法，这里暂时改成只判断主进程，是否是冷启动，其他进程一律非冷启动
            String currentProcessName = ProcessUtils.getCurrentProcessName();
            if (currentProcessName != null) {
                if (currentProcessName.contains(":")) {
                    return false;
                } else {
                    if (!isComputeActivityNum) {
                        isComputeActivityNum = true;
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void appStartDataSenderInner() {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "appStartDataSender: 启动上报");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("isPad", UIUtils.checkPad(ReportUtils.getContext()));
        IEventType eventType = new AppEventType(EventKey.APP_VISIT, params);

        EventDispatch.INSTANCE.onEventNotifier(eventType, null);
    }

    public boolean isCurrentProcessAppForeground() {
        return mAppForegroundReported;
    }

    private void appInDataSender() {
        if (!mAppForegroundReported) {
            mAppForegroundReported = true;
            mListenerMgr.startNotify(IAppEventListener::onAppIn);

        }
    }
    /**
     * app进前台上报
     */
    private void onAppForeground() {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "appInDataSender: 前台上报");
        }
        ReportUtils.getContext().registerReceiver(appBackgroundBroadcast, new IntentFilter(getBackgroundBroadcastAction()));

        timer.startTimer();
        defaultPreferences.edit().putLong(APP_IN_TIME_KEY, SystemClock.uptimeMillis()).apply();
        IEventType eventType = new AppEventType(EventKey.APP_IN, null);

        EventDispatch.INSTANCE.onEventNotifier(eventType, null);
    }

    private final BroadcastReceiver appBackgroundBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ReportUtils.getContext().unregisterReceiver(this);
            onAppBackground();
        }
    };

    /**
     * 当前进程进后台回调
     */
    public void appOutDataSender(final boolean isMainThread) {
        if (mAppForegroundReported) {
            mAppForegroundReported = false;
            mListenerMgr.startNotify(listener -> listener.onAppOut(isMainThread));
        }
    }

    private void onAppBackground(){
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "appOutDataSender: 后台上报");
        }
        timer.stopTimer();
        Map<String, Object> params = new HashMap<>();
        long currentDuration = timer.getDuration();
        if (currentDuration > 500) {
            params.put(InnerReportKeyKt.REPORT_KEY_LVTM, currentDuration - 500);
        } else {
            params.put(InnerReportKeyKt.REPORT_KEY_LVTM, currentDuration);
        }

        IEventType eventType = new AppEventType(EventKey.APP_OUT, params);

        EventDispatch.INSTANCE.onEventNotifier(eventType, null);
        ReferManager.INSTANCE.clearGlobalDPRefer();
    }

    public String getCurrentSessionId() {
        return mUsId;
    }

    public String getLastSessionId() {
        return lastSessionId;
    }

    public static String buildSessionId() {
        int random = new Random().nextInt(900) + 100;
        return System.currentTimeMillis() + "#" + random + "#" + DataReportInner.getInstance().getConfiguration().getVersionInfo();
    }

    public Activity getPreActivity(Activity currentActivity) {
        for (int i = 0; i < activityStack.size(); i++) {
            if (activityStack.get(i).get() == currentActivity) {
                if (i > 0) {
                    while (i > 0) {
                        Activity activityTemp = activityStack.get(i - 1).get();
                        Object isIgnore = DataRWProxy.getInnerParam(activityTemp, InnerKey.VIEW_IGNORE_ACTIVITY);
                        Object isTransparent = DataRWProxy.getInnerParam(activityTemp, InnerKey.VIEW_TRANSPARENT_ACTIVITY);
                        if (!checkFlat(isIgnore) && !checkFlat(isTransparent)) {
                            return activityTemp;
                        }
                        i--;
                    }
                }
            }
        }
        return null;
    }

    private boolean checkFlat(Object flag) {
        return flag instanceof Boolean && ((Boolean) flag);
    }
}
