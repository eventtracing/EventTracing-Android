package com.netease.cloudmusic.datareport.report.exception

import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

abstract class VTreeExceptionInfo(val node: VTreeNode?) : IErrorInfo {

    override fun getContent(): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            put("isPage", node?.isPage()?:false)
            putPrivateParams(this)
        }
    }

    abstract fun putPrivateParams(params: MutableMap<String, Any>)

}