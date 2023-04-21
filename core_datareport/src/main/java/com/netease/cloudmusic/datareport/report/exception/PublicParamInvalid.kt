package com.netease.cloudmusic.datareport.report.exception

import com.netease.cloudmusic.datareport.inner.DataReportInner

/**
 * 公参格式不符合预期
 */
class PublicParamInvalidError(value: String) : RegularExceptionInfo(value) {

    override fun putPrivateParams(params: MutableMap<String, Any>) {
        params.apply {
            put("regx", DataReportInner.getInstance().configuration.patternGlobalKey)
        }
    }

    override val key: String
        get() = "PublicParamInvalid"

    override val code: Int
        get() = 53
}