package com.netease.cloudmusic.datareport.report.exception

import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

/**
 * 节点唯一性检测失败
 */
class NodeNotUniqueError(node: VTreeNode) : VTreeExceptionInfo(node) {

    override fun putPrivateParams(params: MutableMap<String, Any>) {
        params.apply {
            put("spm", node?.getSpm()?:"?")
            put("identifier", node?.hashCode()?.toString() ?: "")
        }
    }

    override val key: String
        get() = "NodeNotUnique"

    override val code: Int
        get() = 41

}