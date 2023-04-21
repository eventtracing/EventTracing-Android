package com.netease.cloudmusic.datareport.report

import android.app.Activity
import android.app.Dialog
import android.text.TextUtils
import android.view.View
import com.netease.cloudmusic.datareport.event.IEventType
import com.netease.cloudmusic.datareport.report.data.ReportDataFactory
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.getVTreeNode
/**
 * 自定义事件上报
 */
object CustomReport {

    private fun reportEvent(eventType: IEventType, vTreeNode: VTreeNode? = null) {
        val eventId = eventType.getEventType()
        val obj = eventType.getTarget()

        if (TextUtils.isEmpty(eventId)) {
            require(!DataReportInner.getInstance().configuration.isDebugMode) { "reportEvent, eventId is empty" }
            return
        }

        // 如果类型为null，则只进行事件和数据上报
        if (obj == null) {
            val finalData = ReportDataFactory.createFinalData(null, eventType).apply {
                this?.eventParams?.get(REFER_TYPE)?.let {
                    ReferManager.onEventUpload(eventType, this)
                }
            }
            FinalDataTarget.handle(finalData)
            return
        }

        // 如果object不是合法参数类型，则直接返回
        if (!checkTrackObjectArgument(obj)) {
            return
        }
        val targetNode = vTreeNode ?: getVTreeNode(obj)
        FinalDataTarget.handle(ReportDataFactory.createFinalData(targetNode, eventType).apply {
            if (targetNode != null) {
                ReferManager.onEventUpload(eventType, this)
            }
        })
    }

    /**
     * 检验外部设置的元素object参数是不是合法的
     *
     * @param object 页面的object参数
     * @return true:合法；false:不合法
     */
    private fun checkElementObjectArgument(obj: Any): Boolean {
        return obj is Dialog || obj is View
    }

    /**
     * 检验外部设置的track中object参数是不是合法的
     *
     * @param obj object对象
     * @return true:合法；false:不合法
     */
    private fun checkTrackObjectArgument(obj: Any): Boolean {
        return checkElementObjectArgument(obj) || obj is Activity
    }

    fun customReportEvent(eventType: IEventType, vTreeNode: VTreeNode? = null) {
        reportEvent(eventType, vTreeNode)
    }


}