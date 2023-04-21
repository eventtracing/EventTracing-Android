package com.netease.cloudmusic.datareport.inject;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.MenuPopupWindow;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.netease.cloudmusic.datareport.R;
import com.netease.cloudmusic.datareport.data.DataRWProxy;
import com.netease.cloudmusic.datareport.event.ClickEventObserver;
import com.netease.cloudmusic.datareport.event.EventTransferPolicy;
import com.netease.cloudmusic.datareport.inject.activity.ScrollFactory;
import com.netease.cloudmusic.datareport.inner.InnerKey;
import com.netease.cloudmusic.datareport.notifier.ListScrolledNotifier;
import com.netease.cloudmusic.datareport.notifier.ViewScrollNotifier;
import com.netease.cloudmusic.datareport.report.refer.ReferManager;
import com.netease.cloudmusic.datareport.vtree.logic.LogicMenuManager;
import com.netease.cloudmusic.datareport.vtree.logic.LogicViewManager;
import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.notifier.EventNotifyManager;
import com.netease.cloudmusic.datareport.notifier.IEventListener;
import com.netease.cloudmusic.datareport.notifier.ListScrollNotifier;
import com.netease.cloudmusic.datareport.notifier.RecyclerViewScrollPositionNotifier;
import com.netease.cloudmusic.datareport.notifier.RecyclerViewSetAdapterNotifier;
import com.netease.cloudmusic.datareport.notifier.ViewPagerSetAdapterNotifier;
import com.netease.cloudmusic.datareport.notifier.ViewReuseNotifier;
import com.netease.cloudmusic.datareport.inject.fragment.FragmentCompat;
import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.vtree.page.DialogListUtil;
import com.netease.cloudmusic.datareport.data.ReusablePool;
import com.netease.cloudmusic.datareport.utils.UIUtils;

import java.lang.reflect.Field;
import java.util.List;

public class EventCollector implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "EventCollector";

    private static class InstanceHolder {

        private static EventCollector sInstance = new EventCollector();

    }

    public static EventCollector getInstance() {
        return InstanceHolder.sInstance;
    }

    private EventCollector() {

    }

    private Field mOwnerRecyclerViewField;

    private Field mLayoutManagerRecyclerViewField;

    private EventNotifyManager mNotifyManager = new EventNotifyManager();

    public void registerEventListener(IEventListener listener) {
        mNotifyManager.registerEventListener(listener);
    }

    public void unregisterEventListener(IEventListener listener) {
        mNotifyManager.unregisterEventListener(listener);
    }

    public void onScrollChanged(View targetView) {
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        ViewScrollNotifier notifier = (ViewScrollNotifier) ReusablePool.obtain(ReusablePool.TYPE_VIEW_SCROLL);
        notifier.init(targetView);
        mNotifyManager.addEventNotifierForDuration(targetView, notifier);
    }

    public void onBehaviorScrollStop(View targetView) {
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        ViewScrollNotifier notifier = (ViewScrollNotifier) ReusablePool.obtain(ReusablePool.TYPE_VIEW_SCROLL);
        notifier.init(targetView);
        mNotifyManager.addEventNotifierForDuration(targetView, notifier);
    }

    public static void onBehaviorScrollStopStatic(View targetView) {
        getInstance().onBehaviorScrollStop(targetView);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "onActivityCreated: activity=" + activity.getClass().getName());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        ReferManager.INSTANCE.onActivityCreated(activity, savedInstanceState);
        mNotifyManager.onActivityCreate(activity);
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityStarted: activity = " + activity.getClass().getName());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onActivityStarted(activity);
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityResumed: activity = " + activity.getClass().getName());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onActivityResumed(activity);
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityPause: activity = " + activity.getClass().getName());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onActivityPaused(activity);
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityStopped: activity=" + activity.getClass().getName());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onActivityStopped(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        ReferManager.INSTANCE.onActivitySaveInstanceState(activity, outState);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onActivityDestroyed: activity=" + activity.getClass().getName());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        DialogListUtil.onActivityDestroy(activity);
        LogicViewManager.INSTANCE.onActivityDestroy(activity);
        mNotifyManager.onActivityDestroyed(activity);
    }

    public void onFragmentResumed(final FragmentCompat fragment) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onFragmentResumed: fragment = " + fragment.getClass().getName() + fragment.hashCode());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onFragmentResumed(fragment);
    }

    public void onFragmentPaused(final FragmentCompat fragment) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onFragmentPaused: fragment = " + fragment.getClass().getName() + fragment.hashCode());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onFragmentPaused(fragment);
    }

    public void onFragmentDestroyView(final FragmentCompat fragment) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onFragmentDestroyView: fragment = " + fragment.getClass().getName() + fragment.hashCode());
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onFragmentDestroyView(fragment);
    }

    public void onDialogFocusChanged(final Dialog dialog, boolean hasFocus) {
        final Activity dialogActivity = DialogListUtil.getDialogActivity(dialog);
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onDialogFocusChanged: dialog = " + dialog.getClass().getName() + ", hasFocus = " + hasFocus + ", activity = " + UIUtils.getActivityInfo(dialogActivity));
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        if (dialogActivity == null) {
            return;
        }
        if (hasFocus) {
            DialogListUtil.onDialogResume(dialogActivity, dialog);
            mNotifyManager.onDialogShow(dialogActivity, dialog);
        } else {
            mNotifyManager.onDialogHide(dialogActivity, dialog);
        }
    }

    public void onDialogStop(final Dialog dialog) {
        final Activity dialogActivity = DialogListUtil.getDialogActivity(dialog);
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onDialogStop: dialog = " + dialog.getClass().getName() + ", activity = " + UIUtils.getActivityInfo(dialogActivity));
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        DialogListUtil.onDialogStop(dialogActivity, dialog);
        mNotifyManager.onDialogHide(dialogActivity, dialog);
    }

    public void onSetRecyclerViewAdapter(final RecyclerView recyclerView) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onSetRecyclerViewAdapter, recyclerView = " + UIUtils.getViewInfo(recyclerView));
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        RecyclerViewSetAdapterNotifier notifier = (RecyclerViewSetAdapterNotifier) ReusablePool.obtain(ReusablePool.TYPE_RECYCLER_VIEW_SET_ADAPTER);
        notifier.init(recyclerView);
        mNotifyManager.addEventNotifier(recyclerView, notifier);
    }

    public static void onSetRecyclerViewAdapterStatic(final RecyclerView recyclerView) {
        getInstance().onSetRecyclerViewAdapter(recyclerView);
    }

    public void onSetViewPagerAdapter(final ViewPager viewPager) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onSetViewPagerAdapter, viewPager = " + UIUtils.getViewInfo(viewPager));
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        ViewPagerSetAdapterNotifier notifier = (ViewPagerSetAdapterNotifier) ReusablePool.obtain(ReusablePool.TYPE_VIEW_PAGER_SET_ADAPTER);
        notifier.init(viewPager);
        mNotifyManager.addEventNotifier(viewPager, notifier);
    }

    public static void onSetViewPagerAdapterStatic(final ViewPager viewPager) {
        getInstance().onSetViewPagerAdapter(viewPager);
    }

    public void onRecyclerViewScrollToPosition(final RecyclerView.LayoutManager layoutManager) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onRecyclerViewScrollToPosition");
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        onRecyclerViewScroll(layoutManager);
    }

    public static void onRecyclerViewScrollToPositionStatic(final RecyclerView.LayoutManager layoutManager) {
        getInstance().onRecyclerViewScrollToPosition(layoutManager);
    }

    public void onRecyclerViewScrollToPositionWithOffset(final RecyclerView.LayoutManager layoutManager) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onRecyclerViewScrollToPositionWithOffset");
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        onRecyclerViewScroll(layoutManager);
    }

    public static void onRecyclerViewScrollToPositionWithOffsetStatic(final RecyclerView.LayoutManager layoutManager) {
        getInstance().onRecyclerViewScrollToPositionWithOffset(layoutManager);
    }

    private void onRecyclerViewScroll(final RecyclerView.LayoutManager layoutManager) {
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        RecyclerView recyclerView = getRecyclerView(layoutManager);
        if (recyclerView != null) {
            RecyclerViewScrollPositionNotifier notifier = (RecyclerViewScrollPositionNotifier) ReusablePool.obtain(ReusablePool.TYPE_RECYCLER_VIEW_SCROLL_POSITION);
            notifier.init(recyclerView);
            mNotifyManager.addEventNotifier(recyclerView, notifier);
        }
    }
    public static void onViewPreClickedStatic(final View view) {
        getInstance().onViewPreClicked(view);
    }

    public void onViewPreClicked(final View view) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.debug(TAG, "onViewPreClicked, view = " + UIUtils.getViewInfo(view));
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        EventTransferPolicy policy = (EventTransferPolicy) DataRWProxy.getInnerParam(view, InnerKey.VIEW_EVENT_TRANSFER);
        View targetView = null;
        if (policy != null && view != null) {
            targetView = policy.getTargetView(view);
        }
        if (targetView == null) {
            targetView = view;
        }

        ClickEventObserver.INSTANCE.onPreClick(targetView);
        ReferManager.INSTANCE.onPreClickEvent(targetView);
    }

    public void onViewClicked(final View view) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.debug(TAG, "onViewClicked, view = " + UIUtils.getViewInfo(view));
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onViewClick(view);
    }

    public void onDialogClicked(DialogInterface dialog, int which) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onDialogClicked, dialog = " + dialog.getClass().getSimpleName() + ", which = " + which);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        // 弹框按钮点击，只能拿到Dialog对象，拿不到View对象。对于业务方来说。也没法在View上绑定元素参数。先不考虑弹框按钮的点击上报
    }

    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onItemClick, parent = " + parent.getClass().getSimpleName() + ", view = " + UIUtils.getViewInfo(view) + ", position = " + position);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onViewClick(view);
    }

    public void onCheckedChanged(final RadioGroup group, int checkedId) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onCheckedChanged, view = " + UIUtils.getViewInfo(group) + ", checkedId = " + checkedId);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onViewClick(group);
    }

    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onCheckedChanged, view = " + UIUtils.getViewInfo(buttonView) + ", isChecked = " + isChecked);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onViewClick(buttonView);
    }

    public void onStopTrackingTouch(final SeekBar seekBar) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onStopTrackingTouch, view = " + UIUtils.getViewInfo(seekBar));
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        mNotifyManager.onViewClick(seekBar);
    }

    public void onListScrollStateChanged(final AbsListView listView, final int scrollState) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onListScrollStateChanged, view = " + UIUtils.getViewInfo(listView) + ", scrollState = " + scrollState);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        ListScrollNotifier notifier = (ListScrollNotifier) ReusablePool.obtain(ReusablePool.TYPE_LIST_SCROLL);
        notifier.init(listView, scrollState);
        mNotifyManager.addEventNotifier(listView, notifier);
    }

    public static void onListScrollStateChangedStatic(final AbsListView listView, final int scrollState) {
        getInstance().onListScrollStateChanged(listView, scrollState);
    }


    public void onListScrolled(final AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }

        ListScrolledNotifier notifier = (ListScrolledNotifier) ReusablePool.obtain(ReusablePool.TYPE_LIST_SCROLLED);
        notifier.init(listView, firstVisibleItem, visibleItemCount, totalItemCount);
        mNotifyManager.addEventNotifier(listView, notifier);
    }

    public static void onListScrolledStatic(final AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        getInstance().onListScrolled(listView, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    public void onListGetView(int position, final View convertView, ViewGroup parent, long itemId) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onListGetView, parent = " + UIUtils.getViewInfo(parent) + ", convertView = " + UIUtils.getViewInfo(convertView) + ", position = " + position);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        if (convertView == null) {
            return;
        }
        ViewReuseNotifier notifier = (ViewReuseNotifier) ReusablePool.obtain(ReusablePool.TYPE_VIEW_REUSE);
        notifier.init(parent, convertView, itemId);
        mNotifyManager.addEventNotifier(convertView, notifier);
    }

    public static void onListGetViewStatic(int position, final View convertView, ViewGroup parent) {
        getInstance().onListGetView(position, convertView, parent, 0);
    }

    public void onRecyclerBindViewHolder(final RecyclerView.ViewHolder holder, int position, long itemId) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onRecyclerBindViewHolder, holder = " + holder.getClass().getSimpleName() + ", position = " + position);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        onRecyclerViewItemReuse(holder, itemId);
    }

    public static void onRecyclerBindViewHolderStatic(final RecyclerView.ViewHolder holder, int position) {
        getInstance().onRecyclerBindViewHolder(holder, position, 0);
    }

    public void onRecyclerBindViewHolder(final RecyclerView.ViewHolder holder, int position, List<Object> payloads, long itemId) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onRecyclerBindViewHolder2, holder = " + holder.getClass().getSimpleName() + ", position = " + position);
        }
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        onRecyclerViewItemReuse(holder, itemId);
    }

    private void onRecyclerViewItemReuse(RecyclerView.ViewHolder holder, long itemId) {
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        ViewReuseNotifier notifier = (ViewReuseNotifier) ReusablePool.obtain(ReusablePool.TYPE_VIEW_REUSE);
        notifier.init(getViewHolderOwnerView(holder), holder.itemView, itemId);
        mNotifyManager.addEventNotifier(holder.itemView, notifier);
    }

    private ViewGroup getViewHolderOwnerView(RecyclerView.ViewHolder holder) {
        if (mOwnerRecyclerViewField == null) {
            try {
                mOwnerRecyclerViewField = RecyclerView.ViewHolder.class.getDeclaredField("mOwnerRecyclerView");
            } catch (NoSuchFieldException e) {
                if (DataReportInner.getInstance().isDebugMode()) {
                    Log.e(TAG, "find no mOwnerRecyclerView field");
                }
            }
        }
        if (mOwnerRecyclerViewField != null) {
            mOwnerRecyclerViewField.setAccessible(true);
            try {
                return (ViewGroup) mOwnerRecyclerViewField.get(holder);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                if (DataReportInner.getInstance().isDebugMode()) {
                    Log.e(TAG, "find no mOwnerRecyclerView field");
                }
            }
        }
        return null;
    }

    private RecyclerView getRecyclerView(RecyclerView.LayoutManager layoutManager) {
        if (mLayoutManagerRecyclerViewField == null) {
            try {
                mLayoutManagerRecyclerViewField = RecyclerView.LayoutManager.class.getDeclaredField("mRecyclerView");
            } catch (NoSuchFieldException e) {
                if (DataReportInner.getInstance().isDebugMode()) {
                    Log.e(TAG, "find no mRecyclerView field");
                }
            }
        }
        if (mLayoutManagerRecyclerViewField != null) {
            mLayoutManagerRecyclerViewField.setAccessible(true);
            try {
                return (RecyclerView) mLayoutManagerRecyclerViewField.get(layoutManager);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                if (DataReportInner.getInstance().isDebugMode()) {
                    Log.e(TAG, "find no mRecyclerView field");
                }
            }
        }
        return null;
    }


    public static void trackViewOnClick(View view) {
        getInstance().onViewClicked(view);
    }

    public static void trackListView(AdapterView<?> adapterView, View view, int position) {
        getInstance().onItemClick(adapterView, view, position, 0);
    }

    public static void trackRadioGroup(RadioGroup view, int checkedId) {
        getInstance().onCheckedChanged(view, checkedId);
    }

    public static void onMenuItemInitializeStatic(MenuView.ItemView itemView, MenuItemImpl itemData) {
        LogicMenuManager.INSTANCE.onMenuItemInitialize(itemView, itemData);
    }

    public static void onMenuPopupShowStatic(MenuPopupWindow popupWindow, MenuBuilder mMenu) {
        LogicMenuManager.INSTANCE.onMenuPopupShow(popupWindow, mMenu); }

    public static void onMenuPopupDismissStatic(MenuPopupWindow popupWindow, MenuBuilder mMenu) {
        LogicMenuManager.INSTANCE.onMenuPopupDismiss(popupWindow, mMenu);
    }

    public static View getScrollView(View preResult, View parent, String name, Context context, AttributeSet attrs) {
        return ScrollFactory.Companion.getScrollView(preResult, parent, name, context, attrs);
    }
}
