package com.netease.cloudmusic.datareport.report.refer

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.event.IEventType
import com.netease.cloudmusic.datareport.event.WebEventType
import com.netease.cloudmusic.datareport.inject.EventCollector
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.cloudmusic.datareport.report.*
import com.netease.cloudmusic.datareport.report.data.FinalData
import com.netease.cloudmusic.datareport.report.data.PageContext
import com.netease.cloudmusic.datareport.report.data.PageContextManager
import com.netease.cloudmusic.datareport.report.data.PageStepManager
import com.netease.cloudmusic.datareport.utils.ReportUtils
import com.netease.cloudmusic.datareport.utils.SPUtils
import com.netease.cloudmusic.datareport.utils.ThreadUtils
import com.netease.cloudmusic.datareport.utils.UIUtils
import com.netease.cloudmusic.datareport.vtree.*
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

/**
 * 用来管理所有的归因链路
 */
object ReferManager : DefaultEventListener() {

    init {
        EventCollector.getInstance().registerEventListener(this)
    }

    private const val HS_REFER_KEY = "hs_refer_id"

    //该key主要用来在内存回收的时候把refer保存到savedInstanceState中
    private const val KEY_ACTIVITY_SAVE_INSTANCE = "data_report_activity_refer_save_instance"

    private const val HS_REFER_PATTERN = "\\[[^\\[\\]]*\\]"

    private val referStorage = ReferStorage()
    private val preReferStorage = PreReferStorage()
    private val mutableReferStorage = MutableReferStorage()


    //埋点子线程
    private fun updateHsRefer(hsRefer: String) {
        SPUtils.put(ReportUtils.getContext(), HS_REFER_KEY, hsRefer)
    }

    /**
     * 生成之后就不会变的 refer，需要在这里维护
     */
    private val psReferMap = WeakHashMap<Activity, MutableMap<Int, String>>()

    private val psSpmReferMap = WeakHashMap<Activity, MutableMap<String, String>>()

    private val psRestoreReferMap = WeakHashMap<Activity, MutableMap<String, String>>()

    /**
     * 构建一条refer字符串的构建类
     */
    class ReferBuilder() {

        companion object {
            private const val SESSION_INDEX = 0
            private const val TYPE_INDEX = 1
            private const val ACTSEQ_INDEX = 2
            private const val PGSTEP_INDEX = 3
            private const val SPM_INDEX = 4
            private const val SCM_INDEX = 5
        }

        val referList = Array(6) { "" }

        private val debugKey = StringBuilder()
        private var option = 0

        fun setSessionId(sessId: String): ReferBuilder {
            if (sessId.isEmpty()) {
                return this
            }
            debugKey.append(REFER_CONST_SESSID).append("|")
            setData(SESSION_INDEX, sessId)
            return this
        }

        fun setType(type: String): ReferBuilder {
            if (type.isEmpty()) {
                return this
            }
            debugKey.append(REFER_CONST_TYPE).append("|")
            setData(TYPE_INDEX, type)
            return this
        }

        fun setActSeq(actSeq: String): ReferBuilder {
            if (actSeq.isEmpty()) {
                return this
            }
            debugKey.append(REFER_CONST_ACTSEQ).append("|")
            setData(ACTSEQ_INDEX, actSeq)
            return this
        }

        fun setPgStep(pgStep: String): ReferBuilder {
            if (pgStep.isEmpty()) {
                return this
            }
            debugKey.append(REFER_CONST_PGSTEP).append("|")
            setData(PGSTEP_INDEX, pgStep)
            return this
        }

        fun setSpm(spm: String): ReferBuilder {
            if (spm.isEmpty()) {
                return this
            }
            debugKey.append(REFER_CONST_SPM).append("|")
            setData(SPM_INDEX, spm)
            return this
        }

        fun setScm(scm: String): ReferBuilder {
            if (scm.isEmpty()) {
                return this
            }
            debugKey.append(REFER_CONST_SCM).append("|")
            setData(SCM_INDEX, scm)
            return this
        }

        fun setFlagER(): ReferBuilder {
            setFlag(REFER_ID_ER, REFER_CONST_ER)
            return this
        }

        fun setFlagUndefined(): ReferBuilder {
            setFlag(REFER_ID_UNDEFINED, REFER_CONST_UNDEFINED)
            return this
        }

        fun setFlagH5(): ReferBuilder {
            setFlag(REFER_ID_H5, REFER_CONST_H5)
            return this
        }

        private fun setFlag(flag: Int, value: String) {
            debugKey.append(value).append("|")
            option = option or (1 shl flag)
        }

        private fun setData(index: Int, value: String) {
            option = option or (1 shl index)
            referList[index] = value
        }

        fun build(): String {
            val tempStr = StringBuilder()
            if (DataReportInner.getInstance().isDebugMode) {
                val dKey = debugKey.toString()
                if (dKey.isNotEmpty()) {
                    tempStr.append("[_dkey:${dKey.substring(0, dKey.length - 1)}]")
                }
            }
            tempStr.append("[F:${option}]")
            referList.forEach {
                if (it.isNotEmpty()) {
                    tempStr.append("[${it}]")
                }
            }
            return tempStr.toString()
        }
    }

    /**
     * 判断当前这个page的曝光是否需要加入到归因
     */
    private fun isPageViewIgnoreRefer(node: VTreeNode): Boolean{
        if (node.getInnerParam(InnerKey.VIEW_REFER_MUTE) == true) {
            return true
        }
        var tempNode: VTreeNode? = node
        while (tempNode?.parentNode != null) {
            if (tempNode.getInnerParam(InnerKey.VIEW_IGNORE_REFER) == true) {
                return true
            }
            tempNode = tempNode.parentNode
        }
        return false
    }

    /**
     * 页面曝光事件
     * 该方法应该在埋点子线程调用
     * @param node 页面节点
     * @param pageViewData 埋点数据
     * @param isRoot 是否是根页面
     */
    fun onPageView(node: VTreeNode, pageViewData: FinalData, isRoot: Boolean) {
        if (isPageViewIgnoreRefer(node)) {
            return
        }

        val oid = node.getOid()
        if (isRoot) {
            referStorage.updateLastPageView(pageViewData)
        }
        if (isContainsHsRefer(oid)) {
            val hsRefer = getReferStr(pageViewData, null)
            if (hsRefer.isNotEmpty()) {
                pageViewData.put(HS_REFER, hsRefer)
            }
            updateHsRefer(hsRefer)
        }
    }

    /**
     * 如果是页面曝光，需要修正refer数据
     * 原因是一些动态数据在生成refer的时候还没有去拿数据
     */
    fun onPageViewFix(pageViewData: FinalData, isRoot: Boolean) {
        if (isRoot) {
            referStorage.getLastPageView()?.let {
                if (it.eventParams[SPM_KEY] == pageViewData.eventParams[SPM_KEY]) {
                    referStorage.updateLastPageView(pageViewData)
                }
            }
        }
        val hsRefer = getHsRefer()
        val tempHsRefer = getReferStr(pageViewData, null)
        val oldArray = getMatch(hsRefer)
        val tempArray = getMatch(tempHsRefer)
        if (oldArray.size == 5 && tempArray.size == 5 && oldArray[0] == tempArray[0] && oldArray[1] == tempArray[1] && oldArray[2] == tempArray[2] && oldArray[3] == tempArray[3]) {
            pageViewData.put(HS_REFER, tempHsRefer)
            updateHsRefer(tempHsRefer)
        }
    }

    private fun getMatch(refer: String):ArrayList<String> {
        val array = arrayListOf<String>()
        val pattern = Pattern.compile(HS_REFER_PATTERN)
        val matcher = pattern.matcher(refer)

        while (matcher.find()) {
            array.add(refer.substring(matcher.start(), matcher.end()))
        }
        return array
    }

    private fun isContainsHsRefer(oid: String?): Boolean {
        if (oid == null) {
            return false
        }
        return DataReportInner.getInstance().configuration.hsReferOidList.contains(oid)
    }

    /**
     * 事件上报触发的方法
     * 在子线程调用
     */
    fun onEventUpload(type: IEventType, eventData: FinalData?) {
        if (type.isContainsRefer() && eventData != null) {
            referStorage.addEventUpload(eventData)
        }
    }

    /**
     * H5发出来的事件，在这里保存
     */
    fun onWebViewEvent(eventType: WebEventType) {
        val runnable = Runnable {
            eventType.getTarget()?.let {
                preReferStorage.addWebViewRefer(it, eventType)
            }
        }
        if (UIUtils.isMainThread()) {
            runnable.run()
        } else {
            ThreadUtils.runOnUiThread(runnable)
        }
    }

    /**
     * rootPage曝光事件
     * 在埋点子线程调用
     */
    fun onRootViewExposure(vTreeNode: VTreeNode) {
        findAttachedActivity(vTreeNode.getNode())?.let {
            val spm = vTreeNode.getSpm()
            val restoreRefer = psRestoreReferMap[it]?.remove(spm)
            if (restoreRefer != null) {
                if (psRestoreReferMap[it]?.isEmpty() == true) {
                    psRestoreReferMap.remove(it)
                }
            }

            var map = psReferMap[it]
            if (map == null) {
                map = mutableMapOf()
                psReferMap[it] = map
            }
            if (!map.containsKey(vTreeNode.hashCode())) {
                val psReferTemp = restoreRefer ?: getPgRefer(vTreeNode.getOid())
                map[vTreeNode.hashCode()] = psReferTemp

                val spmReferMap = psSpmReferMap[it] ?: (mutableMapOf<String, String>().apply { psSpmReferMap[it] = this })
                spmReferMap[spm] = psReferTemp
            }
            if (!isPageViewIgnoreRefer(vTreeNode)) {
                val psRefer = map[vTreeNode.hashCode()]
                mutableReferStorage.onRootExposure(psRefer)
            }
        }
    }

    /**
     * 针对系统内存回收的场景，需要对refer进行特殊的逻辑处理，保证在回复页面之后依然可以拿到之前的refer
     * 内存被回收之后的页面恢复，把refer从savedInstanceState中取出
     *
     */
    fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            return
        }
        val map = psRestoreReferMap[activity] ?: (mutableMapOf<String, String>().apply { psRestoreReferMap[activity] = this })
        val referBundle = savedInstanceState.getBundle(KEY_ACTIVITY_SAVE_INSTANCE)
        referBundle?.keySet()?.forEach {
            referBundle.getString(it)?.apply { map[it] = this }
        }
    }

    /**
     * 内存被系统回收的时候，保存refer信息
     * @see onActivityCreate
     */
    fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        val innerBundle = Bundle()
        psSpmReferMap[activity]?.forEach {
            innerBundle.putString(it.key, it.value)
        }
        outState.putBundle(KEY_ACTIVITY_SAVE_INSTANCE, innerBundle)
    }

    @MainThread
    fun onMainThreadRootView(view: View?) {
        view?.let {
            if (IEventType.isIgnoreRefer(it)) {
                return
            }
            preReferStorage.onRootPageExposure(it)
        }
    }

    @MainThread
    fun getMainThreadPrePsRefer(): String? {
        return preReferStorage.getPrePsRefer()
    }

    internal fun getLastPageView(): FinalData? {
        return referStorage.getLastPageView()
    }

    internal fun updateUndefineTime() {
        referStorage.updateUndefineTime()
    }

    fun getLastUndefineTime(): Long {
        return referStorage.getLastUndefineTime()
    }

    private fun getReferFinalData(oid: String): Pair<FinalData?, Boolean> {
        val eventData = referStorage.getLastEventRefer(oid)
        val pageData = referStorage.getLastPageView()
        val finalData = if (eventData != null && pageData == null) {
            eventData
        } else if (eventData == null && pageData != null) {
            pageData
        } else if (eventData != null && pageData != null) {
            val eventTime = (eventData.eventParams[UPLOAD_TIME] as? Long?) ?: 0
            val pageTime = (pageData.eventParams[UPLOAD_TIME] as? Long?) ?: 0
            if (eventTime >= pageTime) {
                eventData
            } else {
                pageData
            }
        } else {
            null
        }
        val isUndefine = finalData?.let {
            ((it.eventParams[UPLOAD_TIME] as? Long?) ?: 0) < getLastUndefineTime()
        } ?: false

        return Pair(finalData, isUndefine)
    }

    /**
     * 获取pgRefer
     */
    fun getPgRefer(oid: String): String {
        val pair = getReferFinalData(oid)
        return getReferStr(pair.first, { list, pgRefer ->
            for (index in 0 until list.length()) {
                val map = list[index] as? JSONObject?
                if (map != null) {
                    val id = if (map.has(OID_KEY)) map[OID_KEY] as? String? else null
                    if (id != null && isContainsHsRefer(id)) {
                        updateHsRefer(pgRefer)
                        break
                    }
                }
            }
        },  addUndefine = pair.second)
    }

    /**
     * 获取HsRefer
     */
    fun getHsRefer(): String {
        return SPUtils.get(ReportUtils.getContext(), HS_REFER_KEY, "")
    }

    internal fun getReferStr(finalData: FinalData?, block: ((list: JSONArray, pgRefer: String) -> Unit)?, addSessid: Boolean = false, addUndefine: Boolean = false): String {

        val referBuilder = ReferBuilder()

        if (addSessid) {
            referBuilder.setSessionId(AppEventReporter.getInstance().currentSessionId)
        }
        if (addUndefine) {
            referBuilder.setFlagUndefined()
        }

        var pageList: JSONArray? = null
        if (finalData != null) {
            val params = finalData.getEventParams()
            val eventId = params[INNER_EVENT_CODE]
            if (eventId == EventKey.APP_IN) {
                return referBuilder.setType("s").setPgStep(PageStepManager.getCurrentPageStep().toString()).setSpm(EventKey.APP_IN).build()
            } else {
                val elementList = when (val tempElementList = params[ELEMENT_LIST]) {
                    is JSONArray -> {
                        tempElementList
                    }
                    is ArrayList<*> -> {
                        JSONArray(tempElementList)
                    }
                    else -> {
                        null
                    }
                }
                pageList = when (val tempPageList = params[PAGE_LIST]) {
                    is JSONArray -> {
                        tempPageList
                    }
                    is ArrayList<*> -> {
                        JSONArray(tempPageList)
                    }
                    else -> {
                        null
                    }
                }

                val spm = (params[SPM_KEY] ?: params[REFER_SPM_KEY]) as? String? ?:""
                val scm = (params[SCM_KEY] ?: params[REFER_SCM_KEY]) as? String?

                when {
                    isStartWithSpm(spm, elementList) -> {
                        referBuilder.setType("e")
                    }
                    isStartWithSpm(spm, pageList) -> {
                        referBuilder.setType("p")
                    }
                    else -> {
                        params[REFER_TYPE]?.let {
                            referBuilder.setType(it.toString())
                        }
                    }
                }

                val actSeq = params[ACTIOIN_SEQ_KEY] as? Int?
                        ?: PageStepManager.getCurrentGlobalActSeq()
                referBuilder.setActSeq(actSeq.toString())
                val defaultPgStep = PageStepManager.getCurrentPageStep()
                val pgStep = if (pageList != null && pageList.length() > 0) {
                    val map = pageList[pageList.length() - 1] as JSONObject
                    if (map.has(PAGE_STEP_KEY)) {
                        map[PAGE_STEP_KEY] as? Int? ?: defaultPgStep
                    } else {
                        defaultPgStep
                    }
                } else {
                    defaultPgStep
                }

                referBuilder.setPgStep(pgStep.toString()).setSpm(spm).setScm(scm ?: "")

                if (params.containsKey(FLAG_ER)) {
                    referBuilder.setFlagER()
                }

                if (params[REPORT_CONTEXT] == "h5") {
                    referBuilder.setFlagH5()
                }
            }
        }
        val refer = referBuilder.build()
        pageList?.let { block?.invoke(it, refer) }
        return refer
    }

    private fun isStartWithSpm(spm: String, list: JSONArray?): Boolean {
        if (list != null && list.length() > 0) {
            val map = list[0] as JSONObject
            if (map.has(OID_KEY) && spm.startsWith(map[OID_KEY] as String)) {
                return true
            }
        }
        return false
    }

    /**
     * 获取sidRefer
     */
    fun getSidRefer(): String {
        return AppEventReporter.getInstance().lastSessionId
    }


    /**
     * 获取psRefer
     */
    fun getPsRefer(vTreeNode: VTreeNode): String {
        findAttachedActivity(vTreeNode.getNode())?.let {
            return psReferMap[it]?.get(vTreeNode.hashCode()) ?: ""
        }
        return ""
    }

    fun clearData() {
        referStorage.clearData()
        preReferStorage.clear()
        mutableReferStorage.clear()
        updateHsRefer("")
    }

    override fun onActivityDestroyed(activity: Activity) {
        psReferMap.remove(activity)
        psSpmReferMap.remove(activity)
        psRestoreReferMap.remove(activity)
    }

    /**
     * 获取一个view对应的refer，这个必须在主线程执行
     */
    @JvmOverloads
    @MainThread
    fun getReferOnly(view: Any, isUndefine: Boolean = false): String? {
        val referBuilder = ReferBuilder()
        referBuilder.setSessionId(AppEventReporter.getInstance().currentSessionId)
        val type = if (findRelatedPage(getView(view)) == null) {
            if (DataRWProxy.getElementId(view) == null) {
                return null
            } else {
                "e"
            }
        } else "p"
        referBuilder.setType(type)
        if (isUndefine) {
            referBuilder.setFlagUndefined()
        }
        val target = VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(getView(view))
        val actSeq = ExposureEventReport.getActionSeq(target) + 1
        val pgStep = target?.let {
            getRootPageOrRootElement(it)?.let { rootPage ->
                (PageContextManager.getInstance().get(rootPage.hashCode()) as? PageContext?)?.pageStep
            }
        } ?: (PageStepManager.getCurrentPageStep() + 1)
        val spm = DataReportInner.getInstance().getSpmByView(getView(view))
        val scm = DataReportInner.getInstance().getScmByViewForEr(getView(view))
        referBuilder.setActSeq(actSeq.toString()).setPgStep(pgStep.toString()).setSpm(spm).setScm(scm.first)
        if (scm.second) {
            referBuilder.setFlagER()
        }
        return referBuilder.build()
    }

    @MainThread
    fun onPreClickEvent(view: View?) {
        view?.let {
            if (IEventType.isIgnoreRefer(it)) {
                return
            }
            val policy = DataRWProxy.getInnerParam(view, InnerKey.VIEW_REPORT_POLICY) as? ReportPolicy?
            if (policy == null || policy.reportClick) {
                preReferStorage.addPreClickRefer(view)
            }
        }
    }

    fun onPreCustomEvent(event: IEventType?) {
        event?.let {
            preReferStorage.addPreCustomRefer(it)
        }
    }

    @MainThread
    fun getReferByEvent(event: String?): String? {
        if (event == null) {
            return null
        }
        return preReferStorage.getReferWithEvent(event)
    }

    @MainThread
    fun getLastRefer(): String? {
        return preReferStorage.getLastRefer()
    }

    @MainThread
    fun getUndefineRefer(event: String?): String? {
        if (event == null) {
            return null
        }
        return preReferStorage.getUndefineReferWithEvent(event)
    }

    @MainThread
    fun getLastUndefineRefer(): String? {
        return preReferStorage.getUndefineLastRefer()
    }

    fun getMutableRefer(): String {
        return mutableReferStorage.getMutableRefer()
    }

    fun getGlobalDPRefer(): String {
        return preReferStorage.getGlobalDPRefer()
    }

    fun clearGlobalDPRefer() {
        preReferStorage.clearGlobalDPRefer()
    }

}