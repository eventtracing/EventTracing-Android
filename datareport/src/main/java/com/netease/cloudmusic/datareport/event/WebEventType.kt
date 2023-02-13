package com.netease.cloudmusic.datareport.event

import android.view.View
import com.netease.cloudmusic.datareport.report.webReportEvent
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * Web事件类型
 */
class WebEventType (webView: View, private val eventCode: String, private val useForRefer: Boolean, private val  pList: JSONArray?, private val eList: JSONArray?, private val params: JSONObject?, private val spmPosKey: String): IEventType{

    private val node: WeakReference<View> = WeakReference(webView)

    override fun getEventType(): String {
        return eventCode
    }

    override fun getTarget(): Any? {
        return node.get()
    }

    override fun getParams(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        params?.keys()?.forEach {
            map[it] = params.get(it)
        }
        return map
    }

    override fun isContainsRefer(): Boolean {
        return (useForRefer || eventCode == EventKey.PAGE_VIEW) && !IEventType.isIgnoreRefer(getTarget())
    }

    override fun isActSeqIncrease(): Boolean {
        return false
    }

    fun getPList(): JSONArray? {
        return pList
    }

    fun getEList(): JSONArray? {
        return eList
    }

    fun getSpmPosKey(): String {
        return spmPosKey
    }

    override fun reportEvent(node: VTreeNode?) {
        webReportEvent(this, node)
    }

}