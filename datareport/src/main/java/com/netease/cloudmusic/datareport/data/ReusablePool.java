package com.netease.cloudmusic.datareport.data;

import android.util.SparseArray;

import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.notifier.ListScrollNotifier;
import com.netease.cloudmusic.datareport.notifier.ListScrolledNotifier;
import com.netease.cloudmusic.datareport.notifier.RecyclerViewScrollPositionNotifier;
import com.netease.cloudmusic.datareport.notifier.RecyclerViewSetAdapterNotifier;
import com.netease.cloudmusic.datareport.notifier.ViewClickNotifier;
import com.netease.cloudmusic.datareport.notifier.ViewPagerSetAdapterNotifier;
import com.netease.cloudmusic.datareport.notifier.ViewReuseNotifier;
import com.netease.cloudmusic.datareport.notifier.ViewScrollNotifier;
import com.netease.cloudmusic.datareport.report.data.FinalData;
import com.netease.cloudmusic.datareport.utils.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReusablePool {

    public static final int TYPE_LIST_SCROLL = 1;
    public static final int TYPE_LIST_SCROLLED = 10;
    public static final int TYPE_RECYCLER_VIEW_SET_ADAPTER = 2;
    public static final int TYPE_VIEW_CLICK = 3;
    public static final int TYPE_VIEW_PAGER_SET_ADAPTER = 4;
    public static final int TYPE_VIEW_REUSE = 5;
    public static final int TYPE_FINAL_DATA_REUSE = 6;
    public static final int TYPE_RECYCLER_VIEW_SCROLL_POSITION = 7;
    public static final int TYPE_VIEW_SCROLL = 8;

    private static final int MAX_LIST_SIZE = 10;
    private static final String TAG = "ReusablePool";

    private static final SparseArray<List<Object>> POOL = new SparseArray<>();

    public static Object obtain(int reuseType) {
        synchronized (POOL) {
            List<Object> list = POOL.get(reuseType);
            while (list != null && !list.isEmpty()) {
                Object reusable = list.remove(0);
                if (reusable != null) {
                    if (DataReportInner.getInstance().isDebugMode()) {
                        Log.d(TAG, "obtain: reuse, reuseType = " + reuseType);
                    }
                    return reusable;
                }
            }
        }
        Object reusable = createObject(reuseType);
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "obtain: create, reuseType = " + reuseType + ", reusable=" + reusable);
        }
        if (reusable == null) {
            throw new IllegalArgumentException("Reusable reuseType illegal, reuseType = " + reuseType);
        }
        return reusable;
    }

    public static void recycle(Object reusable, int reuseType) {
        List<Object> list;
        synchronized (POOL) {
            list = POOL.get(reuseType);
            if (list == null) {
                list = Collections.synchronizedList(new ArrayList<>());
                POOL.put(reuseType, list);
            }
            if (list.size() < MAX_LIST_SIZE) {
                list.add(reusable);
            }
        }
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "recycle: reuseType = " + reuseType + " list size=" + list.size() + "， reusable=" + reusable);
        }
    }

    private static Object createObject(int type) {
        switch (type) {
            case TYPE_LIST_SCROLL:
                return new ListScrollNotifier();
            case TYPE_LIST_SCROLLED:
                return new ListScrolledNotifier();
            case TYPE_RECYCLER_VIEW_SET_ADAPTER:
                return new RecyclerViewSetAdapterNotifier();
            case TYPE_VIEW_CLICK:
                return new ViewClickNotifier();
            case TYPE_VIEW_PAGER_SET_ADAPTER:
                return new ViewPagerSetAdapterNotifier();
            case TYPE_VIEW_REUSE:
                return new ViewReuseNotifier();
            case TYPE_FINAL_DATA_REUSE:
                return new FinalData();
            case TYPE_RECYCLER_VIEW_SCROLL_POSITION:
                return new RecyclerViewScrollPositionNotifier();
            case TYPE_VIEW_SCROLL:
                return new ViewScrollNotifier();
            default:
                return null;
        }
    }

}
