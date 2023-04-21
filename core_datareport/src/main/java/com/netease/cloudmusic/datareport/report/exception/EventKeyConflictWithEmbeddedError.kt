package com.netease.cloudmusic.datareport.report.exception

/**
 * 事件命名跟内部保留字段冲突
 */
class EventKeyConflictWithEmbeddedError(value: String) : RegularExceptionInfo(value) {

    override fun putPrivateParams(params: MutableMap<String, Any>) {
    }

    override val key: String
        get() = "EventKeyConflictWithEmbedded"

    override val code: Int
        get() = 52
}