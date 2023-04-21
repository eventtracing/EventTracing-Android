package com.netease.cloudmusic.datareport.provider;

import androidx.annotation.IntDef;

/**
 * 给业务使用的启动相关接口
 */
public interface IAppVisitProvider {

    /**
     * 启动方式定义
     */
    @IntDef({StartType.NONE, StartType.ICON, StartType.PUSH})
    @interface StartType {
        int NONE = -1;
        int ICON = 0;
        int PUSH = 1;
    }

    /**
     * 启动方式，见{@link StartType}, 业务可扩展
     */
    int getStartType();

    /**
     * 启动来源
     */
    String getCallFrom();

    /**
     * 启动Scheme，SDK直接上报，业务需要做encode
     */
    String getCallScheme();

}
