package com.netease.cloudmusic.datareport.notifier

import android.view.View
import com.netease.cloudmusic.datareport.data.ReusablePool

/**
 * view的滑动事件的事件通知者
 * 比如ScrollView的滑动事件
 */
class ViewScrollNotifier : IEventNotifier {

    private var scrollView: View? = null

    fun init(view: View) {
        scrollView = view
    }

    override fun notifyEvent(listener: IEventListener?) {
        scrollView?.let { listener?.onViewScroll(it) }
    }

    override fun getReuseType(): Int {
        return ReusablePool.TYPE_VIEW_SCROLL
    }

    override fun reset() {
        scrollView = null
    }
}