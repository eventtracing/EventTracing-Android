package com.netease.cloudmusic.datareport.utils;

import android.content.Context;

import android.app.Application;

import java.lang.reflect.Method;

public class ReportUtils {

    private static volatile Context sContext;

    public static void setContext(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        if (sContext == null) {
            sContext = getCurrentApplication();
        }
        return sContext;
    }

    private static volatile Application sCurrentApplication;
    private static volatile boolean sGetCurrentApplicationChecked;

    /**
     * 通过反射获取当前Application实例，如果反射调用失败，则返回null
     *
     * @return
     */
    private static Application getCurrentApplication() {
        if (!sGetCurrentApplicationChecked) {
            synchronized (ReportUtils.class) {
                if (!sGetCurrentApplicationChecked) {
                    try {
                        Class clazz = Class.forName("android.app.ActivityThread");
                        Method method = clazz.getMethod("currentApplication");
                        sCurrentApplication = (Application) method.invoke(null);
                        if (sCurrentApplication != null) {
                            sGetCurrentApplicationChecked = true;
                        }
                    } catch (Throwable th) {
                        sGetCurrentApplicationChecked = true;
                        th.printStackTrace();
                    }
                }
            }
        }
        return sCurrentApplication;
    }
}
