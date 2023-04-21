package com.netease.cloudmusic.datareport.event

/**
 * 自定义事件上报类型
 */
class CustomEventType(private val eventConfig: EventConfig): IEventType {

    override fun getEventType(): String {
        return eventConfig.getEventId()
    }

    override fun getTarget(): Any? {
        return eventConfig.getTargetObj()
    }


    override fun getParams(): Map<String, Any> {
        return eventConfig.getParams()
    }

    override fun isContainsRefer(): Boolean {
        return eventConfig.isContainsRefer() && !IEventType.isIgnoreRefer(getTarget())
    }

    override fun isActSeqIncrease(): Boolean {
        return eventConfig.isActSeqIncrease()
    }

    override fun isGlobalDPRefer(): Boolean {
        return eventConfig.isGlobalDPRefer()
    }

}