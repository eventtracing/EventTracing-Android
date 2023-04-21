package com.netease.cloudmusic.datareport.report.exception

import com.netease.cloudmusic.datareport.inner.DataReportInner

/**
 * 用户自定义参数命名不规范
 */

class UserParamInvalidError(value: String) : RegularExceptionInfo(value) {

    override fun putPrivateParams(params: MutableMap<String, Any>) {
        params.apply {
            put("regx", DataReportInner.getInstance().configuration.patternCustomKey)
        }
    }

    override val key: String
        get() = "UserParamInvalid"
    override val code: Int
        get() = 54
}