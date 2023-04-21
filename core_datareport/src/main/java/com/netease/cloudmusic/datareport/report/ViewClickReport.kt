package com.netease.cloudmusic.datareport.report

import android.view.View
import com.netease.cloudmusic.datareport.event.IEventType
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.report.data.ReportDataFactory
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.utils.Log
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.getVTreeNode

object ViewClickReport {

    private const val TAG = "ViewClickReport"

    private fun onViewClickEvent(eventType: IEventType, targetView: View){
        if (DataReportInner.getInstance().isDebugMode) {
            Log.debug(TAG, "onViewClickEvent: eventType : ${eventType.getEventType()}")
        }
        val targetNode = getVTreeNode(targetView)
        if (targetNode != null) {
            FinalDataTarget.handle(ReportDataFactory.createFinalData(targetNode, eventType).apply { ReferManager.onEventUpload(eventType, this) })
        }
    }

    fun viewClickEvent(eventType: IEventType) {
        val target = eventType.getTarget()
        if (target is View) {
            onViewClickEvent(eventType, target)
        }
    }

    fun viewClickEventWithNode(eventType: IEventType, vTreeNode: VTreeNode) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.debug(TAG, "viewClickEventWithNode: eventType : ${eventType.getEventType()}")
        }

        FinalDataTarget.handle(ReportDataFactory.createFinalData(vTreeNode, eventType).apply { ReferManager.onEventUpload(eventType, this) })
    }
}