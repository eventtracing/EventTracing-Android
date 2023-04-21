package com.netease.cloudmusic.datareport.report.data

/**
 * VTree Node 的上下文信息
 */
interface IContext {

    fun getExposureTimes(): Long

    fun getParams(): Map<String, Any>
}