package com.netease.cloudmusic.datareport.event

import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.getView

/**
 * 自定义事件监听
 */
object CustomEventObserver {

    fun onCustomEvent(eventType: IEventType) {
        val target = getView(eventType.getTarget())

        if (target != null) {
            val policy = DataRWProxy.getInnerParam(target, InnerKey.VIEW_EVENT_TRANSFER) as? EventTransferPolicy?
            if (policy != null) {
                val targetView = policy.getTargetView(target)
                if (targetView != null) {
                    val eventTypeWrapper = EventTypeWrapper(targetView, eventType)
                    ReferManager.onPreCustomEvent(eventTypeWrapper)
                    EventDispatch.onEventNotifier(eventTypeWrapper, VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(targetView))
                    return
                }
            }
        }

        ReferManager.onPreCustomEvent(eventType)
        EventDispatch.onEventNotifier(eventType, VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(getView(eventType.getTarget())))
    }
}