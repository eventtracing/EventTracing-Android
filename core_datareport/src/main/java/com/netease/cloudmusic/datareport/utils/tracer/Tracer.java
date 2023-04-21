package com.netease.cloudmusic.datareport.utils.tracer;

import androidx.collection.ArrayMap;

import java.util.Map;

/**
 * 用来跟踪一些方法的耗时
 */
public class Tracer {

    private static Map<String, TraceData> sDataMap = new ArrayMap<>();

    public static void begin1(String name) {
        long begin = System.nanoTime();
        TraceData data = fetchData(name);
        data.lastBeginNano = begin;
    }

    public static long end1(String name) {
        long end = System.nanoTime();
        TraceData data = fetchData(name);
        if (data.lastBeginNano == -1) {
            return -1;
        }
        long totalNano = end - data.lastBeginNano;
        data.lastBeginNano = System.nanoTime();
        return totalNano;
    }

    public static void begin(String name) {
        long begin = System.nanoTime();
        TraceData data = fetchData(name);
        data.lastBeginNano = begin;
    }

    public static long end(String name) {
        long end = System.nanoTime();
        TraceData data = fetchData(name);
        if (data.lastBeginNano == -1) {
            return -1;
        }
        data.count++;
        long totalNano = end - data.lastBeginNano;
        data.totalNano += totalNano;
        data.lastBeginNano = -1;
        return totalNano;
    }

    private static TraceData fetchData(String name) {
        TraceData result = sDataMap.get(name);
        if (result == null) {
            result = new TraceData();
            sDataMap.put(name, result);
        }
        return result;
    }

    private static class TraceData {

        int count = 0;
        long totalNano = 0;
        long lastBeginNano = -1;
    }
}
