package com.netease.cloudmusic.datareport.report.refer

import android.content.ContentValues
import android.content.SharedPreferences
import android.view.View
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.event.*
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.provider.IProcessUpdateAction
import com.netease.cloudmusic.datareport.provider.ProcessUpdateManager
import com.netease.cloudmusic.datareport.report.*
import com.netease.cloudmusic.datareport.report.data.PageContext
import com.netease.cloudmusic.datareport.report.data.PageContextManager
import com.netease.cloudmusic.datareport.report.data.PageStepManager
import com.netease.cloudmusic.datareport.utils.SPUtils
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.getRootPageOrRootElement
import com.netease.cloudmusic.datareport.vtree.getView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * 用来管理事件发生之前需要保存的refer
 * 比如一个View的点击事件，在方法的最前面插桩的方法会调用该类的 addPreClickRefer 方法
 * 自定义事件 没有插桩，但也会区分preEvent 和 Event
 */
internal class PreReferStorage {

    companion object {
        private const val PRE_REFER_LIST = "pre_refer_list" //保存refer列表的key
        private const val UNDEFINE_REFER_LIST = "undefine_refer_list" //保存没有埋点的事件的refer列表的key
        private const val LAST_PAGE_REFER = "last_page_refer" //上一个根页面曝光的refer
        private const val GLOBAL_DP_REFER = "global_dp_refer" //上一个全局事件的refer

        // 保存的数据格式：{"refer_key": ${refer}, "time_key": ${time_key}}
        private const val REFER_KEY = "refer_key"
        private const val TIME_KEY = "time_key"

        private const val ADD_PRE_REFER_ACTION = "add_pre_refer_action"
        private const val ADD_UN_DEFINE_PRE_REFER_ACTION = "add_un_define_pre_refer_action"
        private const val ON_ADD_PRE_REFER = "on_add_pre_refer" //refer 对应的信息
        private const val ON_ADD_PRE_REFER_KEY = "on_add_pre_refer_key" //refer 对应的Event
        private const val ON_ADD_UN_DEFINE_PRE_REFER = "on_add_un_define_pre_refer" //同 ON_ADD_PRE_REFER
        private const val ON_ADD_UN_DEFINE_PRE_REFER_KEY = "on_add_un_define_pre_refer_key" //同 ON_ADD_PRE_REFER_KEY

        internal fun registerPreferenceAction() {
            ProcessUpdateManager.registerAction(ADD_PRE_REFER_ACTION, addPreReferAction)
            ProcessUpdateManager.registerAction(ADD_UN_DEFINE_PRE_REFER_ACTION, addUnDefinePreReferAction)
        }

        private val addPreReferAction = object : IProcessUpdateAction {
            override fun doUpdate(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor, values: ContentValues): List<String>? {
                try {
                    val event = values.getAsString(ON_ADD_PRE_REFER_KEY)
                    val itemObj = JSONObject(values.getAsString(ON_ADD_PRE_REFER)?:"{}")
                    if (event == null) {
                        return null
                    }
                    if (event == EventKey.PAGE_VIEW) {
                        editor.putString(LAST_PAGE_REFER, itemObj.toString())
                        return arrayListOf(LAST_PAGE_REFER)
                    }
                    val referMap = JSONObject(sharedPreferences.getString(PRE_REFER_LIST, "{}")!!)
                    referMap.put(event, itemObj)
                    editor.putString(PRE_REFER_LIST, referMap.toString())
                    return arrayListOf(PRE_REFER_LIST)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    values.remove(ON_ADD_PRE_REFER_KEY)
                    values.remove(ON_ADD_PRE_REFER)
                }
                return null
            }
        }
        private val addUnDefinePreReferAction = object : IProcessUpdateAction {
            override fun doUpdate(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor, values: ContentValues): List<String>? {
                try {
                    val event = values.getAsString(ON_ADD_UN_DEFINE_PRE_REFER_KEY)
                    val itemObj = JSONObject(values.getAsString(ON_ADD_UN_DEFINE_PRE_REFER)?:"{}")
                    if (event == null) {
                        return null
                    }
                    val unDefineReferMap = JSONObject(sharedPreferences.getString(UNDEFINE_REFER_LIST, "{}")!!)
                    unDefineReferMap.put(event, itemObj)
                    editor.putString(UNDEFINE_REFER_LIST, unDefineReferMap.toString())
                    return arrayListOf(UNDEFINE_REFER_LIST)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    values.remove(ON_ADD_UN_DEFINE_PRE_REFER_KEY)
                    values.remove(ON_ADD_UN_DEFINE_PRE_REFER)
                }
                return null
            }
        }
    }

    init {
        registerPreferenceAction()
    }

    fun clear() {
        SPUtils.put(PRE_REFER_LIST, "{}")
        SPUtils.put(LAST_PAGE_REFER, "{}")
        SPUtils.put(UNDEFINE_REFER_LIST, "{}")
        clearGlobalDPRefer()
    }

    /**
     * 一个View的点击事件，在方法的最前面插桩的方法会调用该方法
     */
    fun addPreClickRefer(view: View) {
        addEventRefer(view, EventKey.VIEW_CLICK)
    }

    /**
     * 一个根页面的曝光
     */
    fun onRootPageExposure(view: View) {
        addEventRefer(view, EventKey.PAGE_VIEW, ReferManager.getReferOnly(view, true))
    }

    /**
     * 获取上一个refer
     */
    fun getPrePsRefer(): String? {
        try {
            val lastEvent = getLastReferInner()
            var isUndefine = false
            var lastEventObj: JSONObject? = null
            if (lastEvent != null) {
                val referMap = JSONObject(SPUtils.get(PRE_REFER_LIST, "{}"))
                val unDefineReferMap = JSONObject(SPUtils.get(UNDEFINE_REFER_LIST, "{}"))
                val undefine = unDefineReferMap.optJSONObject(lastEvent)
                lastEventObj = referMap.getJSONObject(lastEvent)
                if (undefine?.getLong(TIME_KEY)?:0 > lastEventObj.getLong(TIME_KEY)) {
                    isUndefine = true
                }
            }
            val pageMap = JSONObject(SPUtils.get(LAST_PAGE_REFER, "{}"))
            if (isUndefine || pageMap.optLong(TIME_KEY, 0L) > lastEventObj?.optLong(TIME_KEY) ?: 0) {
                return pageMap.getString(REFER_KEY)
            } else if (lastEventObj != null) {
                return lastEventObj.getString(REFER_KEY)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 全局事件的refer获取
     */
    fun getGlobalDPRefer(): String {
        return SPUtils.get(GLOBAL_DP_REFER, "")
    }
    /**
     * 全局事件的refer添加
     */
    fun putGlobalDPRefer(refer: String) {
        SPUtils.edit().putString(GLOBAL_DP_REFER, refer).apply()
    }
    /**
     * 清除全局事件的refer
     */
    fun clearGlobalDPRefer() {
        SPUtils.edit().putString(GLOBAL_DP_REFER, "").apply()
    }

    private fun addEventRefer(view: Any?, event: String, refer: String? = null) {
        if (view == null) {
            if (refer != null) {
                val itemObj = JSONObject()
                itemObj.put(REFER_KEY, refer)
                itemObj.put(TIME_KEY, System.currentTimeMillis())
                SPUtils.edit().forSyncAction(ADD_PRE_REFER_ACTION).putString(ON_ADD_PRE_REFER, itemObj.toString()).putString(ON_ADD_PRE_REFER_KEY, event).apply()
            }
            return
        }

        val oid = DataRWProxy.getPageId(view) ?: DataRWProxy.getElementId(view)
        if (oid != null) {
            val itemObj = JSONObject()
            itemObj.put(REFER_KEY, refer ?: ReferManager.getReferOnly(view) ?: "")
            itemObj.put(TIME_KEY, System.currentTimeMillis())
            SPUtils.edit().forSyncAction(ADD_PRE_REFER_ACTION).putString(ON_ADD_PRE_REFER, itemObj.toString()).putString(ON_ADD_PRE_REFER_KEY, event).apply()
            return
        }
        val itemObj = JSONObject()
        itemObj.put(REFER_KEY, view.javaClass.name)
        itemObj.put(TIME_KEY, System.currentTimeMillis())
        SPUtils.edit().forSyncAction(ADD_UN_DEFINE_PRE_REFER_ACTION).putString(ON_ADD_UN_DEFINE_PRE_REFER, itemObj.toString())
            .putString(ON_ADD_UN_DEFINE_PRE_REFER_KEY, event).apply()
        if (event != EventKey.PAGE_VIEW) {
            // 改类的所有方法都是在主线程调用的
            // 此处逻辑特殊，需要post 到埋点线程去执行
            EventDispatch.postRunnable(object : Runnable{
                override fun run() {
                    ReferManager.updateUndefineTime()
                }
            })
        }
    }

    /**
     * web事件的refer添加
     */
    fun addWebViewRefer(view: Any, event: WebEventType) {
        if (event.isContainsRefer()) {
            addEventRefer(view, event.getEventType(), getWebRefer(view, event))
        }
    }

    private fun getWebRefer(view: Any, webEventType: WebEventType): String? {
        val referBuilder = ReferManager.ReferBuilder()
        val pList = webEventType.getPList()
        val eList = webEventType.getEList()
        referBuilder.setSessionId(AppEventReporter.getInstance().currentSessionId)
        if (eList != null && eList.length() > 0) {
            referBuilder.setType("e")
        } else {
            referBuilder.setType("p")
        }
        val target = VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(getView(view))
        val actSeq = ExposureEventReport.getActionSeq(target) + 1
        val pgStep = target?.let {
            getRootPageOrRootElement(it)?.let { rootPage ->
                (PageContextManager.getInstance().get(rootPage.hashCode()) as? PageContext?)?.pageStep
            }
        }
        val spm = DataReportInner.getInstance().getSpmByView(getView(view))
        val scm = DataReportInner.getInstance().getScmByViewForEr(getView(view))
        if (scm.second) {
            referBuilder.setFlagER()
        }
        referBuilder.setActSeq(actSeq.toString()).setPgStep(pgStep.toString())

        fixWebEventInner(pList, eList, referBuilder, spm, scm.first, webEventType.getSpmPosKey())
        referBuilder.setFlagH5()

        return referBuilder.build()
    }


    private fun fixWebEventInner(pList: JSONArray?, eList: JSONArray?, referBuilder: ReferManager.ReferBuilder, dataSpm: String, dataScm: String, spmPosKey: String) {
        val referStrategy = DataReportInner.getInstance().configuration.referStrategy

        var spm = dataSpm
        var scm = dataScm
        val block = fun(node: JSONObject){
            val pMap = mutableMapOf<String, Any>()
            node.keys().forEach { key ->
                pMap[key] = node[key] ?: ""
            }
            val oid = pMap[OID_KEY] as? String ?: ""
            val pos = pMap[spmPosKey]
            val spmItem = if(pos == null) "$oid" else "${oid}:${pos}"
            val scmPair = referStrategy?.buildScm(pMap)
            if (scmPair?.second == true) {
                referBuilder.setFlagER()
            }
            val scmItem = scmPair?.first ?: ""
            spm = "${spmItem}|${spm}"
            scm = "${scmItem}|${scm}"
        }

        pList?.let {
            for (index in it.length() - 1 downTo 0) {
                try {
                    block.invoke(it.getJSONObject(index))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        eList?.let {
            for (index in it.length() - 1 downTo 0) {
                try {
                    block.invoke(it.getJSONObject(index))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        referBuilder.setSpm(spm).setScm(scm)
    }

    /**
     * 自定义事件的refer添加
     */
    fun addPreCustomRefer(event: IEventType) {
        if (event.isContainsRefer()) {
            val target = event.getTarget()
            if (target != null) {
                addEventRefer(target, event.getEventType())
            } else {
                event.getParams()[REFER_TYPE]?.let {
                    val globalRefer = getGlobalPreRefer(it.toString(), event)
                    if (event.isGlobalDPRefer()) {
                        putGlobalDPRefer(globalRefer)
                    }
                    addEventRefer(null, event.getEventType(), globalRefer)
                }
            }
        }
    }

    private fun getGlobalPreRefer(referType: String, event: IEventType): String {
        val builder = ReferManager.ReferBuilder()
        builder.setSessionId(AppEventReporter.getInstance().currentSessionId).setType(referType)
                .setActSeq((PageStepManager.getCurrentGlobalActSeq() + 1).toString())
                .setPgStep(PageStepManager.getCurrentPageStep().toString())
        event.getParams().let {
            it[REFER_SPM_KEY]?.let { spm ->
                builder.setSpm(spm.toString())
            }
            it[REFER_SCM_KEY]?.let { scm ->
                builder.setScm(scm.toString())
            }
            if (it.containsKey(FLAG_ER)) {
                builder.setFlagER()
            }
        }

        return builder.build()
    }

    /**
     * 获取上一个事件对应的refer
     * @params event 需要获取的上一个的事件
     */
    fun getReferWithEvent(event: String): String? {
        return try {
            val referMap = JSONObject(SPUtils.get(PRE_REFER_LIST, "{}"))
            referMap.getJSONObject(event).getString(event)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 针对没有埋点的一些事件，比如点击事件。
     */
    fun getUndefineReferWithEvent(event: String): String? {
        try {
            val referMap = JSONObject(SPUtils.get(PRE_REFER_LIST, "{}"))
            val unDefineReferMap = JSONObject(SPUtils.get(UNDEFINE_REFER_LIST, "{}"))
            val undefineJson = unDefineReferMap.getJSONObject(event)
            val jsonObject = referMap.getJSONObject(event)
            if (jsonObject.getLong(TIME_KEY) < undefineJson.getLong(TIME_KEY)) {
                return undefineJson.getString(REFER_KEY)
            }
        } catch (e: Exception) {
        }
        return null
    }

    internal fun getUndefineLastReferTime(): Long {
        try {
            val tempRefer = getLastReferInner()
            if (tempRefer != null) {
                val unDefineReferMap = JSONObject(SPUtils.get(UNDEFINE_REFER_LIST, "{}"))
                val undefine = unDefineReferMap.getJSONObject(tempRefer)
                return undefine.getLong(TIME_KEY)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }

    /**
     * 针对没有埋点的一些事件，比如点击事件。
     * 获取上一个没有埋点的事件
     */
    fun getUndefineLastRefer(): String? {
        try {
            val tempRefer = getLastReferInner()
            if (tempRefer != null) {
                val referMap = JSONObject(SPUtils.get(PRE_REFER_LIST, "{}"))
                val unDefineReferMap = JSONObject(SPUtils.get(UNDEFINE_REFER_LIST, "{}"))
                val undefine = unDefineReferMap.getJSONObject(tempRefer)
                val refer = referMap.getJSONObject(tempRefer)
                if (undefine.getLong(TIME_KEY) > refer.getLong(TIME_KEY)) {
                    return undefine.getString(REFER_KEY)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getLastReferInner(): String? {

        var currentKey: String? = null
        var currentTime: Long = 0L
        try {
            val referMap = JSONObject(SPUtils.get(PRE_REFER_LIST, "{}"))
            referMap.keys().forEach {
                val value = referMap.getJSONObject(it)
                val time = value.optLong(TIME_KEY, 0L)
                if (currentTime < time) {
                    currentTime = time
                    currentKey = it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return currentKey
    }

    /**
     * 获取上一个事件的refer
     */
    fun getLastRefer(): String? {
        try {
            if (getUndefineLastRefer() != null) {
                return ReferManager.getReferStr(ReferManager.getLastPageView(), null, addSessid = true, addUndefine = true)
            }
            val tempRefer: String? = getLastReferInner()
            if (tempRefer != null) {
                val referMap = JSONObject(SPUtils.get(PRE_REFER_LIST, "{}"))
                return referMap.getJSONObject(tempRefer).getString(REFER_KEY)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}