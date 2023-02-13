package com.netease.cloudmusic.datareport.utils.tracer;

import androidx.collection.ArrayMap;

import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.inner.DataReportInner;

import java.util.Map;

public class SimpleTracer {

    private static final String TAG = "SimpleTracer";

    private static final Map<String, Long> BEGIN_MAP = new ArrayMap<>();

    public static void begin(String tag) {
        if (!DataReportInner.getInstance().isDebugMode()) {
            return;
        }
        BEGIN_MAP.put(tag, System.currentTimeMillis());
    }

    public static void end(String tag) {
        if (!DataReportInner.getInstance().isDebugMode()) {
            return;
        }
        Long begin = BEGIN_MAP.remove(tag);
        if (begin == null) {
            return;
        }
        long cost = System.currentTimeMillis() - begin;
        Log.i(TAG, tag + " cost " + cost + " ms.");
    }
}
