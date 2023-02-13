package com.netease.cloudmusic.datareport.scroller;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import com.netease.cloudmusic.datareport.vtree.VTreeManager;
import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener;
import com.netease.cloudmusic.datareport.inject.EventCollector;
import com.netease.cloudmusic.datareport.inner.DataReportInner;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * 本类负责可滑动视图的处理（RecyclerView/ViewPager/AbsListView）
 * 如果发生了notify或者scroll idle，则走一次检测流程
 */
public class ScrollableViewObserver extends DefaultEventListener {

    private static final String TAG = "ScrollableViewObserver";

    private PendingTask mPendingTask = new PendingTask();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ScrollableHelper mScrollableHelper = new ScrollableHelper();

    public static ScrollableViewObserver getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void onViewReused(ViewGroup parent, View view, long newItemId) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onViewReused: parent=" + parent + ", view=" + view);
        }
        if (parent == null) {
            if (DataReportInner.getInstance().isDebugMode()) {
                throw new UnsupportedClassVersionError("RecyclerView.ViewHolder 23 版本以下获取不到所对应RecyclerView对象，请升级RecyclerView版本");
            } else {
                return;
            }
        }
        handleNotify(parent);
    }

    private void handleNotify(ViewGroup parent) {
        if (!isIdle()) {
            return;
        }
        mHandler.removeCallbacks(mPendingTask);
        mPendingTask.setNotifyView(parent);
        mHandler.post(mPendingTask);
    }

    public boolean isIdle() {
        return !mScrollableHelper.isScrolling();
    }

    private static class PendingTask implements Runnable {
        private Set<View> mNotifyViewList = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());

        private void setNotifyView(View notifyView) {
            mNotifyViewList.add(notifyView);
        }

        @Override
        public void run() {
            if (mNotifyViewList.isEmpty()) {
                return;
            }
            for (View notifyView : mNotifyViewList) {
                VTreeManager.INSTANCE.onPageViewVisible(notifyView);
            }
            mNotifyViewList.clear();
        }
    }

    private static class ScrollableHelper extends ScrollStateObserver {

        @Override
        protected void onIdle(View source) {
            if (DataReportInner.getInstance().isDebugMode()) {
                Log.d(TAG, "onIdle: source=" + source);
            }
            VTreeManager.INSTANCE.onPageViewVisible(source);
        }

        @Override
        protected void onScrollUpdate(View source) {

            if (!DataReportInner.getInstance().getConfiguration().isAopScrollEnable()) {
                return;
            }

            View view = DataReportInner.getInstance().getOidParents(source);
            if (view != null) {
                VTreeManager.INSTANCE.onPartialViewVisible(view, false);
            }
        }
    }

    private ScrollableViewObserver() {
        EventCollector.getInstance().registerEventListener(this);
    }

    private static class InstanceHolder {

        private static final ScrollableViewObserver INSTANCE;

        static {
            INSTANCE = new ScrollableViewObserver();
        }
    }
}
