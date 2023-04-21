package com.netease.cloudmusic.datareport.report.exception

import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.vtree.getView

/**
 * 逻辑挂载死循环
 */
class LogicalMountEndlessLoopError(private val child: Any, private val logicParent: Any) : IErrorInfo {

    override val key: String
        get() = "LogicalMountEndlessLoop"

    override val code: Int
        get() = 43

    override fun getContent(): Map<String, Any> {
        val childEntity = DataRWProxy.getDataEntity(child, false)
        val logicEntity = DataRWProxy.getDataEntity(logicParent, false)

        return mutableMapOf<String, Any>().apply {
            val flag = childEntity.innerParams[InnerKey.VIEW_ALERT_FLAG]
            put("autoMount", flag != null)
            put("isPage", childEntity.pageId != null)
            put("oid", childEntity.pageId?:childEntity.elementId?:"")
            put("targetSpm", DataReportInner.getInstance().getSpmByView(getView(logicParent)))
            put("targetOid", logicEntity.pageId?:childEntity.elementId?:"")
        }
    }

}