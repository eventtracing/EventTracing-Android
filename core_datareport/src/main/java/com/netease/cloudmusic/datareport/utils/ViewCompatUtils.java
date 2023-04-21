package com.netease.cloudmusic.datareport.utils;

import android.os.Build;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;

import com.netease.cloudmusic.datareport.inner.DataReportInner;

/**
 * View的兼容工具类
 */
public class ViewCompatUtils {

    private static final ViewCompatBaseImpl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            IMPL = new ViewCompatApi21Impl();
        } else if (Build.VERSION.SDK_INT >= 19) {
            IMPL = new ViewCompatApi19Impl();
        } else if (Build.VERSION.SDK_INT >= 18) {
            IMPL = new ViewCompatApi18Impl();
        } else {
            IMPL = new ViewCompatBaseImpl();
        }
    }

    /**
     * 获取ViewGroup的clipToPadding值
     */
    public static boolean getClipToPadding(ViewGroup viewGroup) {
        return IMPL.getClipToPadding(viewGroup);
    }

    /**
     * 获取ViewGroup的clipChildren值
     */
    public static boolean getClipChildren(ViewGroup viewGroup) {
        return IMPL.getClipChildren(viewGroup);
    }

    /**
     * 判断视图是否attach到了Window上
     */
    public static boolean isAttachedToWindow(View view) {
        return IMPL.isAttachedToWindow(view);
    }

    /**
     * 默认的兼容能力实现，需要通过反射获取字段
     */
    private static class ViewCompatBaseImpl {

        private static final String TAG = "ViewCompatBaseImpl";

        private static final Integer FLAG_CLIP_TO_PADDING = (Integer) ReflectUtils.getField(
                ViewGroup.class, "FLAG_CLIP_TO_PADDING");

        private static final Integer FLAG_CLIP_CHILDREN = (Integer) ReflectUtils.getField(
                ViewGroup.class, "FLAG_CLIP_CHILDREN");

        public boolean getClipToPadding(ViewGroup viewGroup) {
            return hasBooleanFlag(viewGroup, FLAG_CLIP_TO_PADDING);
        }

        public boolean getClipChildren(ViewGroup viewGroup) {
            return hasBooleanFlag(viewGroup, FLAG_CLIP_CHILDREN);
        }

        public boolean isAttachedToWindow(View view) {
            Object attachInfo = ReflectUtils.getField(View.class, "mAttachInfo", view);
            return attachInfo != null;
        }

        private boolean hasBooleanFlag(ViewGroup viewGroup, Integer flag) {
            Integer groupFlags = (Integer) ReflectUtils.getField(ViewGroup.class, "mGroupFlags", viewGroup);
            if (DataReportInner.getInstance().isDebugMode()) {
                Log.d(TAG, "hasBooleanFlag: groupFlags = " + (groupFlags == null ? "null" : groupFlags.toString()) +
                                ", flag = " + (flag == null ? "null" : flag.toString()));
            }
            if (groupFlags == null || flag == null) {
                return false;
            }
            return (groupFlags & flag) == flag;
        }
    }

    /**
     * Api18提供了获取clipChildren的能力
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class ViewCompatApi18Impl extends ViewCompatBaseImpl {

        @Override
        public boolean getClipChildren(ViewGroup viewGroup) {
            return viewGroup.getClipChildren();
        }
    }

    /**
     * Api19提供了获取isAttachedToWindow的能力
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static class ViewCompatApi19Impl extends ViewCompatApi18Impl {
        @Override
        public boolean isAttachedToWindow(View view) {
            return view.isAttachedToWindow();
        }
    }

    /**
     * Api21提供了获取clipToPadding的能力
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static class ViewCompatApi21Impl extends ViewCompatApi19Impl {

        @Override
        public boolean getClipToPadding(ViewGroup viewGroup) {
            return viewGroup.getClipToPadding();
        }
    }
}
