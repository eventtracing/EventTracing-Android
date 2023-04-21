package com.netease.cloudmusic.datareport.provider;

import java.util.Map;

public interface IFormatter {

    /**
     * 自定义元素参数和公参的格式化拼接能力
     *
     * @param publicParams 事件对应的参数及公共参数
     * @param customParams 非公共参数（从元素/页面上收集到的参数）
     * @return 最终的上报数据
     */
    Map<String, Object> formatEvent(Map<String, Object> publicParams, Map<String, Object> customParams);
}
