package com.netease.cloudmusic.datareport.report

import android.text.TextUtils
import com.netease.cloudmusic.datareport.event.WebEventType
import com.netease.cloudmusic.datareport.report.data.ReportDataFactory
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.getVTreeNode

fun webReportEvent(eventType: WebEventType, vTreeNode: VTreeNode? = null) {
    val eventId = eventType.getEventType()
    val obj = eventType.getTarget()

    if (TextUtils.isEmpty(eventId) || obj == null) {
        return
    }

    val targetNode = vTreeNode ?: getVTreeNode(obj)
    FinalDataTarget.handle(ReportDataFactory.createWebViewFinalData(targetNode, eventType).apply {
        ReferManager.onEventUpload(eventType, this)
    })

}