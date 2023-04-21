package com.netease.cloudmusic.datareport.report.exception

import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

/**
 * 节点 spm 唯一检测
 */
class NodeSPMNotUniqueError(node: VTreeNode) : VTreeExceptionInfo(node) {

    override fun putPrivateParams(params: MutableMap<String, Any>) {
        params.apply {
            put("spm", node?.getSpm() ?: "")
        }
    }

    override val key: String
        get() = "NodeSPMNotUnique"

    override val code: Int
        get() = 42

}