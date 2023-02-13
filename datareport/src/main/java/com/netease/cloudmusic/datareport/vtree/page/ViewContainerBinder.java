package com.netease.cloudmusic.datareport.vtree.page;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;

import com.netease.cloudmusic.datareport.notifier.DefaultEventListener;
import com.netease.cloudmusic.datareport.inject.EventCollector;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * 视图和其容器绑定关系查询的类
 * 容器可能是Activity/Dialog
 */
public class ViewContainerBinder extends DefaultEventListener {

    private final WeakHashMap<View, WeakReference<Object>> mBound = new WeakHashMap<>();

    public void bind(View view, Object container) {
        if (view == null) {
            return;
        }
        mBound.put(view, new WeakReference<>(container));
    }

    public Object getBoundContainer(View view) {
        if (view == null) {
            return null;
        }
        WeakReference<Object> reference = mBound.get(view);
        return reference == null ? null : reference.get();
    }

    @Override
    public void onActivityResume(Activity activity) {
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        View decorView = window.getDecorView();
        if (decorView != null) {
            bind(decorView, activity);
        }
    }

    @Override
    public void onDialogShow(Activity activity, Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        View decorView = window.getDecorView();
        if (decorView != null) {
            bind(decorView, dialog);
        }
    }

    public static ViewContainerBinder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private ViewContainerBinder() {
    }

    private void init() {
        EventCollector.getInstance().registerEventListener(this);
    }

    private static class InstanceHolder {

        static final ViewContainerBinder INSTANCE;

        static {
            INSTANCE = new ViewContainerBinder();
            INSTANCE.init();
        }
    }
}
