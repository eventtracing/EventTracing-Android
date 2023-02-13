package com.netease.cloudmusic.datareport.report.exception

interface IErrorInfo {

    val key: String
    val code: Int

    fun getContent(): Map<String, Any>

    fun toInfoMap(): Map<String, Any> {
        val info = mutableMapOf<String, Any>(Pair("key", key), Pair("code", code))
        info["content"] = getContent()
        return info
    }

}