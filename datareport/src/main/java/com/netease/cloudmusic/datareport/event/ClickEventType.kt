package com.netease.cloudmusic.datareport.event

import android.view.View
import com.netease.cloudmusic.datareport.report.viewClickEvent
import com.netease.cloudmusic.datareport.report.viewClickEventWithNode
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import java.lang.ref.WeakReference

/**
 * 点击的上报类型
 */
class ClickEventType(targetView: View) : IEventType {

    private val viewReference = WeakReference<View>(targetView)

    override fun getEventType(): String {
        return EventKey.VIEW_CLICK
    }

    override fun getTarget(): Any? {
        return viewReference.get()
    }

    override fun getParams(): Map<String, Any> {
        return mapOf()
    }

    override fun isContainsRefer(): Boolean {
        return !IEventType.isIgnoreRefer(getTarget())
    }

    override fun isActSeqIncrease(): Boolean {
        return true
    }

    override fun reportEvent(node: VTreeNode?) {
        if (node != null) {
            viewClickEventWithNode(this, node)
        } else {
            viewClickEvent(this)
        }
    }
}