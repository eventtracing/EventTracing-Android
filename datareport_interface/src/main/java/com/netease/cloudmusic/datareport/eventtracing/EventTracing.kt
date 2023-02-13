package com.netease.cloudmusic.datareport.eventtracing

import android.app.Application
import android.net.Uri
import android.view.View
import androidx.annotation.MainThread
import com.netease.cloudmusic.datareport.Configuration
import com.netease.cloudmusic.datareport.eventtracing.ParamBuilder.Companion.DATA_ALG
import com.netease.cloudmusic.datareport.eventtracing.ParamBuilder.Companion.DATA_ID
import com.netease.cloudmusic.datareport.eventtracing.ParamBuilder.Companion.DATA_ID_ER
import com.netease.cloudmusic.datareport.eventtracing.ParamBuilder.Companion.DATA_TRACE_ID
import com.netease.cloudmusic.datareport.eventtracing.ParamBuilder.Companion.DATA_TRP
import com.netease.cloudmusic.datareport.eventtracing.ParamBuilder.Companion.DATA_TYPE
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.operator.IDataReport
import com.netease.cloudmusic.datareport.policy.TransferType
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * 曙光埋点业务层封装，入口
 */
class EventTracing {

    companion object {

        const val PARAMS_MUTABLE_REFER_KEY = "_multirefers"

        fun config(context: Application, dataReport: IDataReport, config: Configuration) {
            DataReport.getInstance().init(dataReport, context, config)
        }

        fun buildSpm(jsonObject: org.json.JSONObject?, oid: String, pos: Int?): String {
            val ctype = if (jsonObject?.has(DATA_TYPE) == true) jsonObject.get(DATA_TYPE) else ""
            val cid = if (jsonObject?.has(DATA_ID) == true) jsonObject.get(DATA_ID) else ""
            val calg = if (jsonObject?.has(DATA_ALG) == true) jsonObject.get(DATA_ALG) else ""
            return "$oid:${pos ?: ""}:${ctype}:${cid}:${calg}"
        }

        fun buildScm(jsonObject: org.json.JSONObject?): String {
            val ctype = if (jsonObject?.isNull(DATA_TYPE) == false) jsonObject.get(DATA_TYPE) else ""
            val cid = if (jsonObject?.isNull(DATA_ID_ER) == false) jsonObject.get(DATA_ID_ER) else {
                if (jsonObject?.isNull(DATA_ID) == false) jsonObject.get(DATA_ID) else ""
            }
            val ctraceId = if (jsonObject?.isNull(DATA_TRACE_ID) == false) jsonObject.get(DATA_TRACE_ID) else ""
            val transparentData = if (jsonObject?.isNull(DATA_TRP) == false) jsonObject.get(DATA_TRP) else ""
            return "${cid}:${ctype}:${ctraceId}:${transparentData}"
        }

        /**
         * 构建SCM
         */
        fun buildScm(map: Map<String, Any>): String {
            return "${map[DATA_ID_ER] ?: map[DATA_ID] ?: ""}:${map[DATA_TYPE] ?: ""}:${map[DATA_TRACE_ID] ?: ""}:${map[DATA_TRP] ?: ""}"
        }

        /**
         * 构建SCM
         */
        fun buildScmByEr(map: Map<String, Any>?): Pair<String, Boolean> {
            map ?: return Pair("", false)
            var isEr = false
            val cid = (map[DATA_ID]?.toString())?.let {
                if (checkEr(it)) {
                    isEr = true
                    Uri.encode(it)
                } else {
                    it
                }
            }

            return Pair("${cid ?: ""}:${map[DATA_TYPE] ?: ""}:${map[DATA_TRACE_ID] ?: ""}:${map[DATA_TRP] ?: ""}", isEr)
        }

        internal fun checkEr(cid: String): Boolean {
            val pattern = Pattern.compile("[\\:\\|\\[\\]]")
            val matcher = pattern.matcher(cid)
            return matcher.find()
        }

        @Deprecated("使用setEventParams")
        fun setEventDynamicParams(event: String, params: MutableMap<String, Any>, eventList: String) {
            val list = eventList.split(",")
            if (list.contains(event) && !params.containsKey(PARAMS_MUTABLE_REFER_KEY)) {
                params[PARAMS_MUTABLE_REFER_KEY] = DataReport.getInstance().multiRefer
            }
        }

        fun setEventParams(event: String, params: MutableMap<String, Any?>, eventList: String) {
            val list = eventList.split(",")
            if (list.contains(event) && !params.containsKey(PARAMS_MUTABLE_REFER_KEY)) {
                params[PARAMS_MUTABLE_REFER_KEY] = DataReport.getInstance().multiRefer
            }
        }

        /**
         * view自己本身没有oid，但是想发自定义事件，可以通过这个方法找到离自己最近的有oid的祖宗
         * 如果view自己本身是有oid的就会返回自己
         * @param view 需要查找有oid的祖宗的view
         * @return 返回祖宗view， 如果view自己本身是有oid的就会返回自己
         */
        fun getOidParents(view: View?): View? {
            return DataReport.getInstance().getOidParents(view)
        }

        /**
         * 从给的view的上下级关系找到相应的oid
         * 注意：这里的oid必须是view的上下级关系。随便给一个oid是筛不出来的
         * @param view
         * @return
         */
        fun getViewByOid(view: View?, oid: String): View? {
            return DataReport.getInstance().getViewByOid(view, oid)
        }

        /**
         * 重新生成VTree，然后会根据新生成的树进行反曝光和曝光操作
         * 这个函数主要用在AOP没有覆盖到的View的可见性变化
         * @param object 变化的View
         */
        fun reBuildVTree(node: Any?) {
            node?.let {
                DataReport.getInstance().reBuildVTree(it)
            }
        }

        /**
         * 对控件进行重新曝光，该控件的所有子控件都会重新曝光
         * 主要就是先标记这个控件，然后重新生成VTree，然后进行反曝光和曝光操作
         * @param views
         */
        fun reExposureView(vararg views: Any) {
            DataReport.getInstance().reExposureView(* views)
        }

        /**
         * 获取一个View的spm
         */
        fun getSpmByView(view: View?): String {
            return DataReport.getInstance().getSpmByView(view)
        }

        /**
         * 获取一个View对应的refer
         */
        @MainThread
        fun getRefer(view: Any?): String? {
            return DataReport.getInstance().getRefer(view)
        }

        @MainThread
        fun getLastRefer(): String? {
            return DataReport.getInstance().lastRefer
        }

        @MainThread
        fun getReferByEvent(event: String): String? {
            return DataReport.getInstance().getReferByEvent(event)
        }

        @MainThread
        fun getUndefineRefer(event: String?): String? {
            return DataReport.getInstance().getUndefineRefer(event)
        }

        @MainThread
        fun getLastUndefineRefer(): String? {
            return DataReport.getInstance().lastUndefineRefer
        }

        fun getMultiRefer(): String? {
            return DataReport.getInstance().multiRefer
        }

        /**
         * 获取当前的pageStep
         * @return
         */
        fun getCurrentPageStep(): Int {
            return DataReport.getInstance().currentPageStep
        }

        /**
         * 把事件转移到其他的view上面
         * @param type 事件转移的类型，有三种
         * TransferPolicyType.TYPE_TARGET_VIEW 直接转移给目标view
         * TransferPolicyType.TYPE_FIND_UP_OID 指定oid，向上查找oid对应的view
         * TransferPolicyType.TYPE_FIND_DOWN_OID 指定oid，向下查找oid对应的view，不建议使用，效率底下，而且结果可能不是你想要的
         * @param targetView 可以为null，只有选择的type为TransferPolicyType.TYPE_TARGET_VIEW时才需要传非null
         * @param targetOid 可以为null, 只有选择的type为TransferPolicyType.TYPE_FIND_UP_OID或TransferPolicyType.TYPE_FIND_DOWN_OID才需要传非null
         */
        fun setEventTransferPolicy(view: View?, @TransferType type: Int, targetView: View?, targetOid: String?) {
            view?.let {
                DataReport.getInstance().setEventTransferPolicy(it, type, targetView, targetOid)
            }
        }

        fun onWebReport(webView: View, eventCode: String, useForRefer: Boolean, pList: JSONArray?, eList: JSONArray?, params: JSONObject?, spmPosKey: String) {
            DataReport.getInstance().onWebReport(webView, eventCode, useForRefer, pList, eList, params, spmPosKey)
        }
    }
}