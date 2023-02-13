package com.netease.cloudmusic.datareport.report.exception

import com.netease.cloudmusic.datareport.inner.DataReportInner

/**
 * 事件命名不规范
 */
class EventKeyInvalidError(value: String) : RegularExceptionInfo(value) {

    override fun putPrivateParams(params: MutableMap<String, Any>) {
        params.apply {
            put("regx", DataReportInner.getInstance().configuration.patternCustomEvent)
        }
    }

    override val key: String
        get() = "EventKeyInvalid"

    override val code: Int
        get() = 51

}