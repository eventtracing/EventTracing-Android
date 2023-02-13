package com.netease.cloudmusic.datareport.event

/**
 * 错误事件上报
 */
class ExposureEventType(private val eventId: String): IEventType {

    override fun getEventType(): String {
        return eventId
    }

    override fun getTarget(): Any? {
        return null
    }

    override fun getParams(): Map<String, Any> {
        return mapOf()
    }

    override fun isContainsRefer(): Boolean {
        return false
    }

    override fun isActSeqIncrease(): Boolean {
        return eventId == EventKey.PAGE_VIEW
    }
}