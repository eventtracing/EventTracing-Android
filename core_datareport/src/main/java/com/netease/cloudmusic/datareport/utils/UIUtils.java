package com.netease.cloudmusic.datareport.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;

/**
 * 处理UI相关的操作
 */

public class UIUtils {

    private static final Rect TEMP_RECT = new Rect();

    public static boolean isMainThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
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


    public static boolean checkPad(Context context) {
        try {
            boolean isPadInner = (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
            double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
            double screenInches = Math.sqrt(x + y); // 屏幕尺寸
            return isPadInner || screenInches >= 7.0;
        } catch (Exception e) {
            return false;
        }
    }

}
