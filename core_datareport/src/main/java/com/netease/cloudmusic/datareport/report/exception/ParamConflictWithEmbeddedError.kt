package com.netease.cloudmusic.datareport.report.exception

/**
 * 参数命名跟内部保留字冲突
 */
class ParamConflictWithEmbeddedError (value: String) : RegularExceptionInfo(value) {

    override fun putPrivateParams(params: MutableMap<String, Any>) {

    }

    override val key: String
        get() = "ParamConflictWithEmbedded"
    override val code: Int
        get() = 55
}