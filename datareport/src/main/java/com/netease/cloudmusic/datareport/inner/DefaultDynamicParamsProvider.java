package com.netease.cloudmusic.datareport.inner;

import com.netease.cloudmusic.datareport.provider.IDynamicParamsProvider;

import java.util.Map;

public class DefaultDynamicParamsProvider implements IDynamicParamsProvider {

    private DefaultDynamicParamsProvider() {
    }

    private static final DefaultDynamicParamsProvider INSTANCE = new DefaultDynamicParamsProvider();

    public static DefaultDynamicParamsProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public void setPublicDynamicParams(Map<String, Object> params) {

    }

    @Override
    public void setEventDynamicParams(String event, Map<String, Object> params) {

    }

    @Override
    public boolean isActSeqIncrease(String event) {
        return false;
    }
}
