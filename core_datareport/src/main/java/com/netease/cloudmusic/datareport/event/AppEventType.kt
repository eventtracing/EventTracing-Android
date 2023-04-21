package com.netease.cloudmusic.datareport.event

/**
 * APP级别事件，包括冷启动，进入前台和进入后台
 */
class AppEventType(private val eventId: String, private val params: Map<String, Any>? = null): IEventType {

    override fun getEventType(): String {
        return eventId
    }

    override fun getTarget(): Any? {
        return null
    }

    override fun getParams(): Map<String, Any> {
        return params ?: mapOf()
    }

    override fun isContainsRefer(): Boolean {
        return eventId == EventKey.APP_IN
    }

    override fun isActSeqIncrease(): Boolean {
        return false
    }

}