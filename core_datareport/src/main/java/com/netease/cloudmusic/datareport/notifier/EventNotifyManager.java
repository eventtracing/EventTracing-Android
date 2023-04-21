package com.netease.cloudmusic.datareport.notifier;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.MainThread;

import com.netease.cloudmusic.datareport.data.ReusablePool;
import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.inject.fragment.FragmentCompat;
import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.utils.tracer.SimpleTracer;
import com.netease.cloudmusic.datareport.utils.ListenerMgr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 目前项目中AOP有两种做法:
 * 1.替换父类，针对系统类的儿子类（不修改孙子类） —— 所有需要AOP的方法均可以被覆写，直接通过EventCollector通知给所有的EventListener
 * 2.插入代码，针对一些系统的接口函数。（包括儿子类和孙子类） —— 针对接口的AOP 或者 对应类存在部分方法没法被覆写
 * 针对第2种AOP，当儿子和孙子类都被插入了同样的代码以后，可能会导致一次系统调用，我们会收到多次事件调用的问题。
 * 因此需要对AOP第二种做法，加一个AOP事件的去重逻辑。 ———— 各种Notiffier来承接，避免子类和父类的多次重复调用
 * <p>
 * 去重做法使用的一个HashMap保存AOP事件的触发对象，然后立即做真正的通知。在下一个Loop里面去HashMap里面清除对象
 * 那么在前一个Loop里面如果发生了多次事件调用，判断HashMap里面的数据，把后面的事件过滤掉。
 */

public class EventNotifyManager {

    private static final String TAG = "EventNotifyManager";

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private Map<String, IEventNotifier> mNotifierMap = new HashMap<>();

    private ListenerMgr<IEventListener> mListenerMgr = new ListenerMgr<>();

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyEvent();
        }
    };

    public void registerEventListener(IEventListener listener) {
        mListenerMgr.register(listener);
    }

    public void unregisterEventListener(IEventListener listener) {
        mListenerMgr.unregister(listener);
    }

    public void addEventNotifierForDuration(Object key, IEventNotifier notifier) {
        String mapKey = key == null ? String.valueOf(notifier.getReuseType()) : (key.hashCode() + "_" + notifier.getReuseType());
        synchronized (EventNotifyManager.this) {
            IEventNotifier oldNotifier = mNotifierMap.get(mapKey);
            if (oldNotifier != null) {
                oldNotifier.reset();
                ReusablePool.recycle(oldNotifier, oldNotifier.getReuseType());
                mNotifierMap.put(mapKey, notifier);
            } else {
                mNotifierMap.put(mapKey, notifier);
                mMainHandler.post(mNotifyRunnable);
            }
        }
    }

    public void addEventNotifier(Object key, IEventNotifier notifier) {
        addEventNotifierInner(key, notifier, 0);
    }

    private void addEventNotifierInner(Object key, IEventNotifier notifier, long duration) {
        String mapKey = key == null ? String.valueOf(notifier.getReuseType()) : (key.hashCode() + "_" + notifier.getReuseType());
        synchronized (EventNotifyManager.this) {
            IEventNotifier oldNotifier = mNotifierMap.get(mapKey);
            if (oldNotifier != null) {
                oldNotifier.reset();
                ReusablePool.recycle(oldNotifier, oldNotifier.getReuseType());
            }
            mNotifierMap.put(mapKey, notifier);
        }
        mMainHandler.removeCallbacks(mNotifyRunnable);
        if (duration <= 0) {
            mMainHandler.post(mNotifyRunnable);
        } else {
            mMainHandler.postDelayed(mNotifyRunnable, duration);
        }
    }

    private void notifyEvent() {
        HashMap<String, IEventNotifier> copyMap;
        synchronized (EventNotifyManager.this) {
            if (mNotifierMap.isEmpty()) {
                return;
            }
            copyMap = new HashMap<>(mNotifierMap);
            mNotifierMap.clear();
        }
        for (final IEventNotifier notifier : copyMap.values()) {
            if (DataReportInner.getInstance().isDebugMode()) {
                Log.i(TAG, "notifyEvent, notifier = " + notifier.getClass().getSimpleName());
            }
            mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
                @Override
                public void onNotify(IEventListener listener) {
                    notifier.notifyEvent(listener);
                }
            });
            notifier.reset();
            ReusablePool.recycle(notifier, notifier.getReuseType());
        }
        copyMap.clear();
    }

    private final Set<Integer> pendingClickList = new HashSet<>();
    @MainThread
    public void onViewClick(final View view) {
        if (view == null) {
            return;
        }
        if (pendingClickList.contains(view.hashCode())) {
            return;
        }
        pendingClickList.add(view.hashCode());
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                pendingClickList.remove(view.hashCode());
            }
        });
        String tag = "EventNotifyManager.onViewClick(" + view.getClass().getSimpleName() + ")";
        SimpleTracer.begin(tag);
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onViewClick(view);
            }
        });
        SimpleTracer.end(tag);
    }

    public void onActivityCreate(final Activity activity) {
        String tag = "EventNotifyManager.onActivityCreate(" + activity.getClass().getSimpleName() + ")";
        SimpleTracer.begin(tag);
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onActivityCreate(activity);
            }
        });
        SimpleTracer.end(tag);
    }

    public void onActivityStarted(final Activity activity) {
        String tag = "EventNotifyManager.onActivityStarted(" + activity.getClass().getSimpleName() + ")";
        SimpleTracer.begin(tag);
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onActivityStarted(activity);
            }
        });
        SimpleTracer.end(tag);
    }

    public void onActivityResumed(final Activity activity) {
        String tag = "EventNotifyManager.onActivityResume(" + activity.getClass().getSimpleName() + ")";
        SimpleTracer.begin(tag);
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onActivityResume(activity);
            }
        });
        SimpleTracer.end(tag);
    }

    public void onActivityPaused(final Activity activity) {
        String tag = "EventNotifyManager.onActivityPaused(" + activity.getClass().getSimpleName() + ")";
        SimpleTracer.begin(tag);
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onActivityPause(activity);
            }
        });
        SimpleTracer.end(tag);
    }

    public void onActivityStopped(final Activity activity) {
        String tag = "EventNotifyManager.onActivityStopped(" + activity.getClass().getSimpleName() + ")";
        SimpleTracer.begin(tag);
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onActivityStopped(activity);
            }
        });
        SimpleTracer.end(tag);
    }

    public void onActivityDestroyed(final Activity activity) {
        String tag = "EventNotifyManager.onActivityDestroyed(" + activity.getClass().getSimpleName() + ")";
        SimpleTracer.begin(tag);
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onActivityDestroyed(activity);
            }
        });
        SimpleTracer.end(tag);
    }

    public void onFragmentResumed(final FragmentCompat fragment) {
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onFragmentResume(fragment);
            }
        });
    }

    public void onFragmentPaused(final FragmentCompat fragment) {
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onFragmentPause(fragment);
            }
        });
    }

    public void onFragmentDestroyView(final FragmentCompat fragment) {
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onFragmentDestroyView(fragment);
            }
        });
    }

    public void onDialogShow(final Activity dialogActivity, final Dialog dialog) {
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onDialogShow(dialogActivity, dialog);
            }
        });
    }

    public void onDialogHide(final Activity dialogActivity, final Dialog dialog) {
        mListenerMgr.startNotify(new ListenerMgr.INotifyCallback<IEventListener>() {
            @Override
            public void onNotify(IEventListener listener) {
                listener.onDialogHide(dialogActivity, dialog);
            }
        });
    }
}
