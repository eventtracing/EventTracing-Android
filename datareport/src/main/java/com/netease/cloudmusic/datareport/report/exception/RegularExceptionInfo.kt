package com.netease.cloudmusic.datareport.report.exception

abstract class RegularExceptionInfo(val value: String) : IErrorInfo {

    override fun getContent(): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            put("value", value)
            putPrivateParams(this)
        }
    }

    abstract fun putPrivateParams(params: MutableMap<String, Any>)
}