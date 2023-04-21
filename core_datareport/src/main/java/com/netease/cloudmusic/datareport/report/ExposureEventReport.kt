package com.netease.cloudmusic.datareport.report

import android.os.*
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.event.EventDispatch
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.event.ExposureEventType
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.report.data.*
import com.netease.cloudmusic.datareport.vtree.exposure.VTreeExposureManager
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.utils.ThreadUtils
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.exposure.DefaultExposureListener
import com.netease.cloudmusic.datareport.vtree.getRootPageOrRootElement
import com.netease.cloudmusic.datareport.vtree.isRootPage
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 曝光事件上报者
 */
object ExposureEventReport: DefaultExposureListener() {
    private const val EXPOSURE_KEY = 0
    private const val DIS_EXPOSURE_KEY = 1

    /**
     * 交互深度，每个根节点维护一个
     */
    private val actionSeqMap = ConcurrentHashMap<Int, Int>()

    private var handler: ExposureHandler? = null

    private fun initHandler() {
        if (handler == null) {
            handler = ExposureHandler(Looper.myLooper()!!)
        }
    }

    internal class ExposureHandler(looper: Looper): Handler(looper){
        private val exposureNodes = HashSet<VTreeNode>()

        fun addExposureNode(vTreeNode: VTreeNode) {
            exposureNodes.add(vTreeNode)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                EXPOSURE_KEY -> {
                    (msg.obj as? VTreeNode?)?.let {
                        if(exposureNodes.remove(it)){
                            doUploadElementExposure(it)
                        }
                    }
                }
                DIS_EXPOSURE_KEY -> {
                    (msg.obj as? VTreeNode?)?.let {
                        if (!exposureNodes.remove(it)) {
                            doUploadElementDisappear(it)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun postElementView(vTreeNode: VTreeNode, time: Long) {
        initHandler()
        handler?.addExposureNode(vTreeNode)
        handler?.sendMessageDelayed(Message.obtain(handler, EXPOSURE_KEY, vTreeNode), time)
    }

    private fun getTime(vTreeNode: VTreeNode): Any? {
        return null
        //这里把曝光时间的逻辑干掉了
        //return vTreeNode.getInnerParam(InnerKey.VIEW_EXPOSURE_MIN_TIME) ?: DataReportInner.getInstance().configuration.exposureMinTime
    }

    override fun onElementView(vTreeNode: VTreeNode) {
        if (!ReportHelper.reportExposure(vTreeNode)) {
            return
        }
        ElementContextManager[vTreeNode.hashCode()] = ElementContext(SystemClock.uptimeMillis())

        val time = getTime(vTreeNode)
        if (time is Long) {
            postElementView(vTreeNode, time)
            return
        }
        doUploadElementExposure(vTreeNode)
    }

    private fun doUploadElementExposure(vTreeNode: VTreeNode){
        doExposureCallback(EventKey.ELEMENT_VIEW, vTreeNode)
        val finalData = ReportDataFactory.createFinalData(vTreeNode, ExposureEventType(EventKey.ELEMENT_VIEW))
        FinalDataTarget.handle(finalData)
    }

    private fun postElementDisappear(vTreeNode: VTreeNode) {
        initHandler()
        handler?.sendMessage(Message.obtain(handler, DIS_EXPOSURE_KEY, vTreeNode))
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
        val time = getTime(vTreeNode)
        if (time is Long) {
            postElementDisappear(vTreeNode)
            return
        }
        doUploadElementDisappear(vTreeNode)
    }

    override fun onPageView(vTreeNode: VTreeNode, pgStepTemp: VTreeExposureManager.PgStepTemp) {
        var isRootPage = false
        if (dealWithRootVTreeNode(vTreeNode)) {
            val psRefer = ReferManager.getPsRefer(vTreeNode)
            PageContextManager.getInstance().set(vTreeNode, vTreeNode.hashCode(), PageContext(
                    ++pgStepTemp.pgStep,
                    SystemClock.uptimeMillis(),
                    ReferManager.getPgRefer(vTreeNode.getOid()),
                    psRefer))

            isRootPage = true
        } else {
            val option = getReferConsumeOption(vTreeNode)
            if (option != null) {
                val userEvent = (option and InnerKey.REFER_CONSUME_OPTION_EVENT) == InnerKey.REFER_CONSUME_OPTION_EVENT
                val userSubPage = (option and InnerKey.REFER_CONSUME_OPTION_SUB_PAGE) == InnerKey.REFER_CONSUME_OPTION_SUB_PAGE
                ReferManager.onSubPageExposure(vTreeNode, userEvent, userSubPage)
                val subPsRefer = ReferManager.getSubPsRefer(vTreeNode)
                PageContextManager.getInstance().set(vTreeNode, vTreeNode.hashCode(), PageContext(
                    ++pgStepTemp.pgStep,
                    SystemClock.uptimeMillis(),
                    ReferManager.getSubPgRefer(vTreeNode.getOid(), userEvent, userSubPage),
                    subPsRefer))
            } else {
                PageContextManager.getInstance().set(vTreeNode, vTreeNode.hashCode(), PageContext(++pgStepTemp.pgStep, SystemClock.uptimeMillis()))
            }
        }
        if (!ReportHelper.reportExposure(vTreeNode)) {
            return
        }
        doExposureCallback(EventKey.PAGE_VIEW, vTreeNode)
        ReportDataFactory.createFinalData(vTreeNode, ExposureEventType(EventKey.PAGE_VIEW))?.let {
            ReferManager.onPageView(vTreeNode, it, isRootPage)
            FinalDataTarget.handle(vTreeNode.getOid(), it, isRootPage)
        }
    }

    private fun getReferConsumeOption(vTreeNode: VTreeNode): Int? {
        return vTreeNode.getInnerParam(InnerKey.VIEW_REFER_CONSUME_OPTION) as? Int
    }

    override fun onPageDisappear(vTreeNode: VTreeNode) {
        actionSeqMap.remove(vTreeNode.hashCode())
        ReferManager.onPageDisappear(vTreeNode)
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
        if (isRootPage(vTreeNode)) {
            ReferManager.onRootViewExposure(vTreeNode)
            return true
        }
        return false
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