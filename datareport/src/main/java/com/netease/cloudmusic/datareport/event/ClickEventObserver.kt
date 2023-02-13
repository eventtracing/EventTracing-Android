package com.netease.cloudmusic.datareport.event

import android.view.View
import androidx.annotation.MainThread
import com.netease.cloudmusic.datareport.event.EventDispatch.onEventNotifier
import com.netease.cloudmusic.datareport.inject.EventCollector
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.cloudmusic.datareport.report.ReportHelper
import com.netease.cloudmusic.datareport.utils.Log
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.getView
import java.util.*

/**
 * 点击事件监听
 * 这里有两种点击类型
 * 1。 插桩到点击方法的头部位置
 * 2。 插桩到点击方法的尾部位置
 */
object ClickEventObserver : DefaultEventListener() {

    private const val TAG = "ElementClickReporter"

    private val viewTreeMap = WeakHashMap<View, VTreeNode>()

    @MainThread
    fun onPreClick(clickView: View?) {
        if (clickView == null) {
            return
        }
        val eventTransferPolicy = DataRWProxy.getInnerParam(clickView, InnerKey.VIEW_EVENT_TRANSFER) as? EventTransferPolicy?
        val view = if (eventTransferPolicy != null) {
            eventTransferPolicy.getTargetView(clickView) ?: clickView
        } else {
            clickView
        }

        if (!viewTreeMap.containsKey(view)) {
            val oid = DataRWProxy.getPageId(view) ?: DataRWProxy.getElementId(view)
            val policy = DataRWProxy.getInnerParam(view, InnerKey.VIEW_REPORT_POLICY) as? ReportPolicy?
            if (oid != null && (policy == null || policy.reportClick)) {
                VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(getView(view))?.let { viewTreeMap[view] = it }
            }
        }
    }

    override fun onViewClick(clickView: View?) {

        if (clickView == null) {
            return
        }

        val policy = DataRWProxy.getInnerParam(clickView, InnerKey.VIEW_EVENT_TRANSFER) as? EventTransferPolicy?
        val view = if (policy != null) {
            policy.getTargetView(clickView) ?: clickView
        } else {
            clickView
        }

        if (!ReportHelper.reportClick(view)) {
            if (DataReportInner.getInstance().isDebugMode) {
                Log.debug(TAG, "onViewClick not allow: view = $view")
            }
            return
        }
        if (DataReportInner.getInstance().isDebugMode) {
            Log.debug(TAG, "onViewClick: view=$view")
        }
        onEventNotifier(ClickEventType(view), viewTreeMap.remove(view))
    }

    init {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "init ")
        }
        EventCollector.getInstance().registerEventListener(this)
    }

}
