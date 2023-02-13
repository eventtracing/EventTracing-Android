package com.netease.cloudmusic.datareport.report.data;

import androidx.collection.ArrayMap;

import java.util.Map;

/**
 * 最终需要上报的数据
 */
public class FinalData {

    public Map<String, Object> eventParams = new ArrayMap<>();

    public FinalData() {

    }

    public void put(String key, Object value) {
        if (key == null) {
            return;
        }
        if (value == null) {
            value = "";
        }
        eventParams.put(key, value);
    }

    public void putAll(Map<String, ?> objectMap) {
        if (objectMap != null) {
            eventParams.putAll(objectMap);
        }
    }

    public Map<String, Object> getEventParams() {
        return eventParams;
    }

    public void reset() {
        eventParams.clear();
    }
}
