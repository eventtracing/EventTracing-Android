package com.netease.cloudmusic.datareport.report

import android.os.*
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.event.EventDispatch
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.event.ExposureEventType
import com.netease.cloudmusic.datareport.report.data.*
import com.netease.cloudmusic.datareport.vtree.exposure.VTreeExposureManager
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.utils.ThreadUtils
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.exposure.DefaultExposureListener
import com.netease.cloudmusic.datareport.vtree.getRootPageOrRootElement
import com.netease.cloudmusic.datareport.vtree.isRootPage
import java.util.concurrent.ConcurrentHashMap

/**
 * 曝光事件上报者
 */
object ExposureEventReport: DefaultExposureListener() {

    /**
     * 交互深度，每个根节点维护一个
     */
    private val actionSeqMap = ConcurrentHashMap<Int, Int>()

    override fun onElementView(vTreeNode: VTreeNode) {
        if (!ReportHelper.reportExposure(vTreeNode)) {
            return
        }
        ElementContextManager[vTreeNode.hashCode()] = ElementContext(SystemClock.uptimeMillis())
        doUploadElementExposure(vTreeNode)
    }

    private fun doUploadElementExposure(vTreeNode: VTreeNode){
        doExposureCallback(EventKey.ELEMENT_VIEW, vTreeNode)
        val finalData = ReportDataFactory.createFinalData(vTreeNode, ExposureEventType(EventKey.ELEMENT_VIEW))
        FinalDataTarget.handle(finalData)
    }

    private fun doUploadElementDisappear(vTreeNode: VTreeNode) {
        actionSeqMap.remove(vTreeNode.hashCode())
        doDisExposureCallback(EventKey.ELEMENT_DISAPPEAR, vTreeNode)
        val finalData = ReportDataFactory.createFinalData(vTreeNode, ExposureEventType(EventKey.ELEMENT_DISAPPEAR))
        val elementContext = ElementContextManager.remove(vTreeNode.hashCode())
        finalData?.put(EXPOSURE_DURATION, SystemClock.uptimeMillis() - (elementContext?.getExposureTimes()?:0))
        FinalDataTarget.handle(finalData)
    }

    override fun onElementDisappear(vTreeNode: VTreeNode) {
        if (!ReportHelper.reportExposure(vTreeNode) || !ReportHelper.reportElementExposureEnd(vTreeNode)) {
            ElementContextManager.remove(vTreeNode.hashCode())
            return
        }
        doUploadElementDisappear(vTreeNode)
    }

    override fun onPageView(vTreeNode: VTreeNode, pgStepTemp: VTreeExposureManager.PgStepTemp) {
        var isRootPage = false
        if (dealWithRootVTreeNode(vTreeNode)) {
            ReferManager.onRootViewExposure(vTreeNode)
            val psRefer = ReferManager.getPsRefer(vTreeNode)
            PageContextManager.getInstance().set(vTreeNode, vTreeNode.hashCode(), PageContext(
                    ++pgStepTemp.pgStep,
                    SystemClock.uptimeMillis(),
                    ReferManager.getPgRefer(vTreeNode.getOid()),
                    psRefer))

            isRootPage = true
        } else {
            PageContextManager.getInstance().set(vTreeNode, vTreeNode.hashCode(), PageContext(++pgStepTemp.pgStep, SystemClock.uptimeMillis()))
        }
        if (!ReportHelper.reportExposure(vTreeNode)) {
            return
        }
        doExposureCallback(EventKey.PAGE_VIEW, vTreeNode)
        ReportDataFactory.createFinalData(vTreeNode, ExposureEventType(EventKey.PAGE_VIEW))?.let {
            ReferManager.onPageView(vTreeNode, it, isRootPage)
            FinalDataTarget.handleInPageExposure(it, isRootPage)
        }
    }

    override fun onPageDisappear(vTreeNode: VTreeNode) {
        actionSeqMap.remove(vTreeNode.hashCode())
        if (!ReportHelper.reportExposure(vTreeNode)) {
            PageContextManager.getInstance().remove(vTreeNode, vTreeNode.hashCode())
            return
        }
        doDisExposureCallback(EventKey.PAGE_DISAPPEAR, vTreeNode)
        val finalData = ReportDataFactory.createFinalData(vTreeNode, ExposureEventType(EventKey.PAGE_DISAPPEAR))
        val pageContext = PageContextManager.getInstance().remove(vTreeNode, vTreeNode.hashCode()) as? PageContext?
        finalData?.put(EXPOSURE_DURATION, SystemClock.uptimeMillis() - (pageContext?.getExposureTimes()?:0))
        FinalDataTarget.handle(finalData)
    }

    /**
     * 通过当前的节点找到根节点，再通过根节点找到对应的actionSeq
     * @param vTreeNode 查询的节点
     */
    fun getActionSeq(vTreeNode: VTreeNode?): Int {
        if (vTreeNode == null) {
            return 0
        }
        val rootNode = getRootPageOrRootElement(vTreeNode)
        return actionSeqMap[rootNode.hashCode()] ?: 0
    }
    /**
     * 通过当前的节点找到根节点，再通过根节点找到对应的actionSeq
     * 然后对actionSeq做自增加操作
     * @param vTreeNode 查询的节点
     */
    fun getIncreaseActionSeq(vTreeNode: VTreeNode): Int {
        val rootNode = getRootPageOrRootElement(vTreeNode)
        var actionSeq = actionSeqMap[rootNode.hashCode()] ?: 0
        actionSeqMap[rootNode.hashCode()] = ++actionSeq

        return actionSeq
    }

    private fun dealWithRootVTreeNode(vTreeNode: VTreeNode): Boolean {
        return isRootPage(vTreeNode)
    }

    private fun doExposureCallback(event: String, vTreeNode: VTreeNode) {
        ThreadUtils.runOnUiThread {
            DataRWProxy.getExposureCallback(vTreeNode.getNode())?.onExposure(event, vTreeNode.getOid(), vTreeNode.getNode(), vTreeNode.visibleRect)
        }
        EventDispatch.callbackEvent(event, vTreeNode)
    }

    private fun doDisExposureCallback(event: String, vTreeNode: VTreeNode) {
        ThreadUtils.runOnUiThread{
            DataRWProxy.getExposureCallback(vTreeNode.getNode())?.onDisExposure(event, vTreeNode.getOid(), vTreeNode.getNode())
        }
        EventDispatch.callbackEvent(event, vTreeNode)
    }

    init {
        VTreeExposureManager.registerListener(this)
    }

}