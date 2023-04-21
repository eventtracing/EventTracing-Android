package com.netease.cloudmusic.datareport.utils;

import android.app.Application;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

public class ProcessUtils {
    private static String currentProcessName;

    /**
     * 获取当前进程的名称
     */
    @Nullable
    public static String getCurrentProcessName() {
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName;
        }
        currentProcessName = getCurrentProcessNameByApplication();
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName;
        }
        currentProcessName = getCurrentProcessNameByActivityThread();
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName;
        }
        currentProcessName = getCurrentProcessNameByProc();
        return currentProcessName;
    }

    private static String getCurrentProcessNameByApplication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }
        return null;
    }

    private static String getCurrentProcessNameByActivityThread() {
        String processName = null;
        try {
            final Method declaredMethod = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader())
                    .getDeclaredMethod("currentProcessName");
            declaredMethod.setAccessible(true);
            final Object invoke = declaredMethod.invoke(null);
            if (invoke instanceof String) {
                processName = (String) invoke;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return processName;
    }

    private static String getCurrentProcessNameByProc() {
        BufferedReader br = null;
        String processName = "";
        try {
            br = new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/" + "cmdline"));
            processName = br.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return processName;
    }
}