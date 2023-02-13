package com.netease.cloudmusic.datareport.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Looper;
import android.view.View;
import android.widget.AbsListView;

/**
 * 处理UI相关的操作
 */

public class UIUtils {

    private static final Rect TEMP_RECT = new Rect();

    public static boolean isMainThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static String getViewInfo(View view) {
        if (view == null) {
            return "null";
        }
        String idStr = "0";
        int id = view.getId();
        if (id != View.NO_ID) {
            try {
                Context context = ReportUtils.getContext();
                if (context != null) {
                    idStr = context.getResources().getResourceName(id);
                }
            } catch (Resources.NotFoundException ignore) {
            }
        }

        return view.getClass().getSimpleName() + ":" + idStr;
    }

    public static String getActivityInfo(Activity activity) {
        if (activity == null) {
            return "null";
        }
        return activity.getClass().getSimpleName();
    }

    public static Object getListScrollListener(AbsListView listView) {
        return listView == null ? null : ReflectUtils.getListField(AbsListView.class, "mOnScrollListener", listView);
    }

    public static double getViewExposureRate(View view) {
        if (view == null) {
            return 0;
        }
        boolean visible = view.isShown() && view.getGlobalVisibleRect(TEMP_RECT);
        if (!visible) {
            return 0;
        }
        long exposureArea = (long) TEMP_RECT.width() * TEMP_RECT.height();
        long realArea = (long) view.getWidth() * view.getHeight();
        return exposureArea * 1.0 / realArea;
    }
}
