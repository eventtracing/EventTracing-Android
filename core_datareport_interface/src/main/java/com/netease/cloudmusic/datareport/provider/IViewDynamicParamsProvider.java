package com.netease.cloudmusic.datareport.provider;

import androidx.annotation.Nullable;

import java.util.Map;

/**
 * 获取元素动态参数的接口。最终上报时SDK从业务侧取，保证取值的有效。
 */
public interface IViewDynamicParamsProvider {
    @Nullable
    Map<String, Object> getViewDynamicParams();
}
