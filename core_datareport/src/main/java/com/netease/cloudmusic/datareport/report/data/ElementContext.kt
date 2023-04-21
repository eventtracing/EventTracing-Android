package com.netease.cloudmusic.datareport.report.data

/**
 * 元素上下文信息
 */
class ElementContext(private val exposureTime: Long): IContext {

    override fun getExposureTimes(): Long {
        return exposureTime
    }

    override fun getParams(): Map<String, Any> {
        return mutableMapOf()
    }

}