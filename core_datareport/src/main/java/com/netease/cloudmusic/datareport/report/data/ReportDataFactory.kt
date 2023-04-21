package com.netease.cloudmusic.datareport.report.data

import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.data.ReusablePool
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.event.IEventType
import com.netease.cloudmusic.datareport.event.WebEventType
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.report.*
import com.netease.cloudmusic.datareport.report.ExposureEventReport.getIncreaseActionSeq
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.findActivityByAny
import org.json.JSONArray
import org.json.JSONException
import java.lang.ref.WeakReference

object ReportDataFactory {

    private fun fixWebEventInner(eventData: FinalData, list: JSONArray, listKey: String, spmPosKey: String) {
        val referStrategy = DataReportInner.getInstance().configuration.referStrategy

        var spm = eventData.eventParams[SPM_KEY] as? String ?: ""
        var scm = eventData.eventParams[SCM_KEY] as? String ?: ""
        val dataList = eventData.eventParams[listKey] as? MutableList<Map<String?, Any?>> ?: (mutableListOf<Map<String?, Any?>>().apply { eventData.put(listKey, this) })

        for (index in list.length() - 1 downTo 0) {
            val pNode = try {
                list.getJSONObject(index)
            } catch (e: JSONException) {
                e.printStackTrace()
                continue
            }
            val pMap = mutableMapOf<String, Any>()
            pNode.keys().forEach { key ->
                pMap[key] = pNode[key] ?: ""
            }
            val oid = pMap[OID_KEY] as? String ?: ""
            val pos = pMap[spmPosKey]
            val spmItem = if(pos == null) "$oid" else "${oid}:${pos}"
            val scmPair = referStrategy?.buildScm(pMap)
            if (scmPair?.second == true) {
                eventData.put(FLAG_ER, "1")
            }
            val scmItem = scmPair?.first ?: ""
            spm = "${spmItem}|${spm}"
            scm = "${scmItem}|${scm}"
            dataList.add(0, mutableMapOf<String?, Any?>().apply { putAll(pMap) })
        }
        if (spm.endsWith("|")) {
            spm = spm.substring(0, spm.length - 1)
        }
        if (scm.endsWith("|")) {
            scm = scm.substring(0, scm.length - 1)
        }
        eventData.put(SPM_KEY, spm)
        eventData.put(SCM_KEY, scm)
    }

    /**
     * 构建web产生的数据
     */
    fun createWebViewFinalData(inPg: VTreeNode?, eventType: WebEventType): FinalData? {
        val eventData = createFinalData(inPg, eventType, mapOf(REPORT_CONTEXT to "h5"))
        if (eventData == null) {
            return eventData
        }
        if (eventType.getEventType() == EventKey.PAGE_VIEW) {
            val pgStep = PageStepManager.getCurrentPageStep() + 1
            PageStepManager.setCurrentPageStep(pgStep)
            eventType.getPList()?.let {
                if (it.length() > 0) {
                    it.getJSONObject(0).put(PAGE_STEP_KEY, pgStep)
                }
            }
        }
        eventType.getPList()?.let { list ->
            fixWebEventInner(eventData, list, PAGE_LIST, eventType.getSpmPosKey())
        }
        eventType.getEList()?.let { list ->
            fixWebEventInner(eventData, list, ELEMENT_LIST, eventType.getSpmPosKey())
        }
        return eventData
    }

    /**
     * 通过VTreeNode 构建数据
     */
    fun createFinalData(vTreeNode: VTreeNode?, eventType: IEventType): FinalData? {
        return createFinalData(vTreeNode, eventType, null)
    }

    private fun createFinalData(vTreeNode: VTreeNode?, eventType: IEventType, otherParams: Map<String, Any>?): FinalData? {
        val finalData = ReusablePool.obtain(ReusablePool.TYPE_FINAL_DATA_REUSE) as FinalData
        checkEmptySetKeyValue(finalData, INNER_EVENT_CODE, eventType.getEventType())
        checkEmptySetKeyValue(finalData, SESSION_ID, AppEventReporter.getInstance().currentSessionId)
        checkEmptySetKeyValue(finalData, SIDE_REFER, ReferManager.getSidRefer())
        checkEmptySetKeyValue(finalData, HS_REFER, ReferManager.getHsRefer())
        checkEmptySetKeyValue(finalData, GLOBAL_DB_REFER, ReferManager.getGlobalDPRefer())
        finalData.put(UPLOAD_TIME, System.currentTimeMillis())
        finalData.putAll(eventType.getParams())

        if (vTreeNode == null && eventType.isContainsRefer()) {
            val globalActSeq = PageStepManager.getCurrentGlobalActSeq() + 1
            finalData.put(ACTIOIN_SEQ_KEY, globalActSeq)
            PageStepManager.setCurrentGlobalActSeq(globalActSeq)
        }

        vTreeNode?.let {
            if (DataReportInner.getInstance().configuration.dynamicParamsProvider.isActSeqIncrease(eventType.getEventType()) ||
                eventType.isActSeqIncrease() || eventType.isContainsRefer()) {
                val actSeq = getIncreaseActionSeq(it)
                if (eventType.getEventType() == EventKey.PAGE_VIEW) {
                    (PageContextManager.getInstance()[it.hashCode()] as? PageContext?)?.apply { this.actSeq = actSeq}
                }
                finalData.put(ACTIOIN_SEQ_KEY, actSeq)
            }

            it.getInnerParam(InnerKey.VIEW_TO_OID)?.let { oid ->
                finalData.put(TO_OID, oid)
            }
            if (!getLinkParams(eventType.getEventType(), vTreeNode, finalData)) {
                return null
            }
        }
        otherParams?.let { finalData.putAll(it) }
        DataReportInner.getInstance().configuration.syncDynamicParamsProvider?.setEventDynamicParams(eventType.getEventType(), finalData.getEventParams())
        findActivityByAny(vTreeNode?.getNode())?.let {
            finalData.put("Activity", it::class.java.name)
        }

        return finalData
    }

    private fun getLinkParams(event: String, inPg: VTreeNode, finalData: FinalData): Boolean {
        val referStrategy = DataReportInner.getInstance().configuration.referStrategy
        val spmBuilder = StringBuilder()
        val scmBuilder = StringBuilder()
        val elementList = mutableListOf<Map<String?, Any?>>()
        val pageList = mutableListOf<Map<String?, Any?>>()

        var isAlreadyPage = false

        var currentNode = inPg
        while (currentNode.parentNode != null) {
            val itemMap = mutableMapOf<String?, Any?>()
            val oid = currentNode.getOid()
            val pos = currentNode.getPos()
            spmBuilder.append(oid)
            pos?.let {
                spmBuilder.append(":").append(pos)
                //itemMap[POS_KEY] = it 不再输出pos，用业务方设置的s_position替代
            }
            spmBuilder.append("|")
            val innerCustomParams = currentNode.getParams()
            val scmPair = referStrategy?.buildScm(innerCustomParams)
            scmBuilder.append(scmPair?.first ?: "").append("|")
            if (scmPair?.second == true) {
                checkEmptySetKeyValue(finalData, FLAG_ER, "1")
            }

            val isPage = currentNode.isPage()
            if (isPage) {
                if (!isAlreadyPage) {
                    isAlreadyPage = true
                }
            } else {
                if (isAlreadyPage) {
                    currentNode = currentNode.parentNode!!
                    continue
                }
            }

            checkEmptySetKeyValue(itemMap, OID_KEY, oid)
            checkEmptySetKeyValue(itemMap, EXPOSURE_RATIO, currentNode.getExposureRate().toString())
            innerCustomParams?.let {
                itemMap.putAll(it)
            }

            itemMap[CURRENT_NODE_TEMP_KEY] = currentNode.getViewDynamicParams()
            if (currentNode == inPg) {
                currentNode.getEventParams(event).let {
                    itemMap[CURRENT_EVENT_PARAMS] = WeakReference(it)
                }
            }

            if (isPage) {
                itemMap.putAll(currentNode.context?.getParams()
                        ?: PageContextManager.getInstance()[currentNode.hashCode()]?.getParams()
                        ?: mapOf())
                pageList.add(itemMap)
            } else {
                itemMap.putAll(currentNode.context?.getParams()
                        ?: ElementContextManager[currentNode.hashCode()]?.getParams() ?: mapOf())
                elementList.add(itemMap)
            }
            currentNode = currentNode.parentNode!!
        }
        if (elementList.size > 0 && pageList.size == 0) {
            return false
        }
        checkEmptySetKeyValue(finalData, ELEMENT_LIST, elementList)
        checkEmptySetKeyValue(finalData, PAGE_LIST, pageList)
        if (spmBuilder.isEmpty()) {
            checkEmptySetKeyValue(finalData, SPM_KEY, "")
        } else {
            checkEmptySetKeyValue(finalData, SPM_KEY, spmBuilder.substring(0, spmBuilder.length - 1))
        }
        if (scmBuilder.isEmpty()) {
            checkEmptySetKeyValue(finalData, SCM_KEY, "")
        } else {
            checkEmptySetKeyValue(finalData, SCM_KEY, scmBuilder.substring(0, scmBuilder.length - 1))
        }
        return true
    }

    private fun checkEmptySetKeyValue(finalData: FinalData, key: String, value: List<Map<String?, Any?>>?) {
        if (value != null && value.isNotEmpty()) {
            finalData.put(key, value)
        } else {
            finalData.put(key, ArrayList<Map<String?, Any?>>())
        }
    }

    private fun checkEmptySetKeyValue(finalData: FinalData, key: String, value: String?) {
        if (value != null && value.isNotEmpty()) {
            finalData.put(key, value)
        } else {
            finalData.put(key, "")
        }
    }

    private fun checkEmptySetKeyValue(finalData: MutableMap<String?, Any?>, key: String, value: String?) {
        if (value != null && value.isNotEmpty()) {
            finalData[key] = value
        } else {
            finalData[key] = ""
        }
    }

}