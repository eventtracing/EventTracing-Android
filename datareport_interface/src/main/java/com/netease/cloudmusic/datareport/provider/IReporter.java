package com.netease.cloudmusic.datareport.provider;

import java.util.Map;

/**
 * 实际上报行为能力外置
 */
public interface IReporter {
    void report(String event, Map<String, Object> eventParams);
}
