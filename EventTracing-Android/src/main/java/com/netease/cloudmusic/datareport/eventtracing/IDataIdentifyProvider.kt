package com.netease.cloudmusic.datareport.eventtracing

/**
 * 提供数据的唯一性判断的接口
 */
interface IDataIdentifyProvider {
    /**
     * 获取唯一标示
     */
    fun getIdentify(): String
}