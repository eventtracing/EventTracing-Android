package com.netease.cloudmusic.datareport.provider;

import com.netease.cloudmusic.datareport.event.EventKey;

import java.util.Map;

/**
 * 事件动态参数接口，包括公共参数, actseq是否自增的钩子
 */
public interface IDynamicParamsProvider {

    /**
     * 设置公共动态参数
     *
     * @param params 动态参数
     */
    void setPublicDynamicParams(Map<String, Object> params);

    /**
     * 设置事件动态参数
     *
     * @param event  事件类型，见{@link EventKey}里面定义的类型
     * @param params 动态参数
     */
    void setEventDynamicParams(String event, Map<String, Object> params);

    /**
     * 给业务方传一个event 的，来判断这个key是否自增
     * @param event
     * @return
     */
    boolean isActSeqIncrease(String event);
}
