package com.netease.cloudmusic.datareport.vtree

import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.inner.DataReportInner
import org.json.JSONArray
import org.json.JSONObject

//打印生成的VTree的结构

fun logVTreeJSON(vTreeNode: VTreeNode?, tag: String? = null) {

    val logger = DataReportInner.getInstance().configuration.logger;
    if (!DataReportInner.getInstance().isDebugMode || logger == null) {
        return
    }

    vTreeNode ?: return

    val jsonObject = JSONObject()
    putJSONNode(vTreeNode, jsonObject)

    DataReportInner.getInstance().configuration.reporter.report("vtree", mapOf(Pair("vtree", jsonObject.toString())))

    logger.d(tag?:"logVTreeJSON", jsonObject.toString())
}

fun logVTreeJSON(vTreeMap: VTreeMap, tag: String? = null) {

    val logger = DataReportInner.getInstance().configuration.logger;
    if (!DataReportInner.getInstance().isDebugMode || logger == null) {
        return
    }

    val vTreeNode = vTreeMap.vTreeNode ?: return

    val jsonObject = JSONObject()
    putJSONNode(vTreeNode, jsonObject)

    DataReportInner.getInstance().configuration.reporter.report("vtree", mapOf(Pair("vtree", jsonObject.toString())))

    logger.d(tag?:"logVTreeJSON", jsonObject.toString())
}

fun putJSONNode(vTreeNode: VTreeNode, jsonObject: JSONObject) {
    jsonObject.put("oid", vTreeNode.getOid())
    jsonObject.put("type", if(!vTreeNode.isPage()) "e" else "p")
    jsonObject.put("isvisible", vTreeNode.isVisible)
    jsonObject.put("rect", vTreeNode.visibleRect)
    jsonObject.put("isVirtual", vTreeNode.isVirtualNode())

    val customParams = vTreeNode.getParams()
    if(customParams != null){
        jsonObject.put("custom", JSONObject(customParams as Map<*, *>))
    }
    if (vTreeNode.childrenList.size > 0) {
        val childrenArray = JSONArray()
        for (child in vTreeNode.childrenList) {
            val childJSONObject = JSONObject()
            putJSONNode(
                child,
                childJSONObject
            )
            childrenArray.put(childJSONObject)
        }
        jsonObject.put("children", childrenArray)
    }
}