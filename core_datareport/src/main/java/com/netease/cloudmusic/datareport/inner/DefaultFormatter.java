package com.netease.cloudmusic.datareport.inner;

import com.netease.cloudmusic.datareport.provider.IFormatter;

import java.util.HashMap;

import java.util.Map;

public class DefaultFormatter implements IFormatter {

    private DefaultFormatter() {
    }

    private static final DefaultFormatter INSTANCE = new DefaultFormatter();

    public static DefaultFormatter getInstance() {
        return INSTANCE;
    }

    @Override
    public Map<String, Object> formatEvent(Map<String, Object> publicParams, Map<String, Object> customParams) {

        Map<String, Object> map = new HashMap<>();
        if (publicParams != null) {
            map.putAll(publicParams);
        }
        map.putAll(customParams);
        return map;
    }
}
