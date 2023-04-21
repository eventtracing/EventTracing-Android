package com.netease.cloudmusic.datareport.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.netease.cloudmusic.datareport.provider.ProcessPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SPUtils {
    private static final String DATA_REPORT_PROFILE = "data_report_profile";
    public static final String ALL_PROCESS_KEY = "all_process_key";
    public static final String DATA_REPORT_DEBUG_UI = "data_report_debug_ui";
    public static ProcessPreferences sp;

    public static void register(SharedPreferences.OnSharedPreferenceChangeListener listener, ArrayList<String> list) {
        Context context = ReportUtils.getContext();
        if (context != null) {
            ((ProcessPreferences)init(context)).registerOnSharedPreferenceChangeListener(listener, list);
        }
    }

    private static SharedPreferences init(@NonNull Context context) {
        if (sp == null) {
            return sp = ProcessPreferences.Companion.getInstance(context, DATA_REPORT_PROFILE);
        }
        return sp;
    }

    public static <E> void put(@NonNull String key, @NonNull E value) {
        Context context = ReportUtils.getContext();
        if (context != null) {
            put(context, key, value);
        }
    }

    public static ProcessPreferences.TreasureEditor edit() {
        return (ProcessPreferences.TreasureEditor)init(ReportUtils.getContext()).edit();
    }

    /**
     * 存
     *
     * @param context 上下文信息
     * @param key     键
     * @param value   值，泛型，自动根据值进行处理
     */
    public static <E> void put(@NonNull Context context, @NonNull String key, @NonNull E value) {
        SharedPreferences.Editor editor = init(context).edit();
        if (value instanceof String || value instanceof Integer || value instanceof Boolean ||
                value instanceof Float || value instanceof Long || value instanceof Double) {
            editor.putString(key, String.valueOf(value));
        }
        editor.apply();
    }

    public static void putSet(@NonNull Context context, @NonNull String key, @NonNull Set<String> value) {
        init(context).edit().putStringSet(key, value).apply();
    }

    public static Set<String> getSet(@NonNull String key) {
        Context context = ReportUtils.getContext();
        if (context != null) {
            return init(context).getStringSet(key, new HashSet<String>());
        }
        return new HashSet<String>();
    }

    @NonNull
    public static <E> E get(@NonNull String key, @NonNull E defaultValue) {
        Context context = ReportUtils.getContext();
        if (context != null) {
            return get(context, key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 取
     *
     * @param context      上下文信息
     * @param key          键
     * @param defaultValue 默认值
     * @return 保存的值
     */
    @NonNull
    public static <E> E get(@NonNull Context context, @NonNull String key, @NonNull E defaultValue) {
        String value = init(context).getString(key, String.valueOf(defaultValue));
        if (defaultValue instanceof String) {
            return (E) value;
        }
        if (defaultValue instanceof Integer) {
            return (E) Integer.valueOf(value);
        }
        if (defaultValue instanceof Boolean) {
            return (E) Boolean.valueOf(value);
        }
        if (defaultValue instanceof Float) {
            return (E) Float.valueOf(value);
        }
        if (defaultValue instanceof Long) {
            return (E) Long.valueOf(value);
        }
        if (defaultValue instanceof Double) {
            return (E) Double.valueOf(value);
        }
        return defaultValue;
    }
}
