package com.netease.cloudmusic.datareport.inner;

import android.util.Log;

import com.netease.cloudmusic.datareport.provider.IReporter;

import org.json.JSONObject;

import java.util.Map;

public class DefaultReporter implements IReporter {

    private DefaultReporter() {
    }

    private static final DefaultReporter INSTANCE = new DefaultReporter();

    public static DefaultReporter getInstance() {
        return INSTANCE;
    }

    @Override
    public void report(String event, Map<String, Object> eventParams) {
        Log.i("DefaultReporter", new JSONObject(eventParams).toString());
    }
}
