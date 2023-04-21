package com.netease.cloudmusic.datareport.event

import android.view.View
import java.lang.ref.WeakReference

/**
 * 事件类型的封装类
 */
class EventTypeWrapper(targetView: View, private val eventType: IEventType) : IEventType {

    private val view = WeakReference<View>(targetView)

    override fun getEventType(): String {
        return eventType.getEventType()
    }

    override fun getTarget(): Any? {
        return view.get()
    }

    override fun getParams(): Map<String, Any> {
        return eventType.getParams()
    }

    override fun isContainsRefer(): Boolean {
        return eventType.isContainsRefer()
    }

    override fun isActSeqIncrease(): Boolean {
        return eventType.isActSeqIncrease()
    }

}