package com.netease.cloudmusic.datareport.event

import android.view.View
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.scroller.ScrollInfo
import com.netease.cloudmusic.datareport.vtree.VTreeManager

/**
 * 滑动事件监听
 */
object ScrollEventObserver {

    fun onScrollEvent(scrollView: View, scrollInfo: ScrollInfo) {

        val view = DataReportInner.getInstance().getOidParents(scrollView)
        view?.let {
            val policy = DataRWProxy.getInnerParam(it, InnerKey.VIEW_EVENT_TRANSFER) as? EventTransferPolicy?
            if (policy != null) {
                val targetView = policy.getTargetView(it)
                if (targetView != null) {
                    VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(targetView)?.let { tree ->
                        EventDispatch.onEventNotifier(ScrollEventType(targetView, scrollInfo), tree)
                    }
                    return
                }
            }

            VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(it)?.let { tree ->
                EventDispatch.onEventNotifier(ScrollEventType(view, scrollInfo), tree)
            }
        }
    }

}