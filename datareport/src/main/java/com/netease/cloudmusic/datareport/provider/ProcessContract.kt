package com.netease.cloudmusic.datareport.provider

import android.net.Uri
import com.netease.cloudmusic.datareport.utils.ReportUtils

internal class ProcessContract {
    companion object{

        const val QUERY_GET = "get"
        const val QUERY_GET_ALL = "getall"
        const val QUERY_CONTAINS = "contains"
        const val PARAM_CLEAR = "clear"
        const val PARAM_IMMEDIATELY = "immediately"
        const val PARAM_SYNC_ACTION = "action"
        const val UPDATE = "update"
        const val REGISTER = "register"
        const val UNREGISTER = "unregister"

        private var sAuthority: String? = null
        private var sAuthorityUri: Uri? = null

        fun getAuthority(): String {
            if (sAuthority == null) {
                sAuthority = "${ReportUtils.getContext()?.packageName?:""}.datareport.preferences"
            }
            return sAuthority!!
        }

        @Synchronized
        fun getAuthorityUri(): Uri {
            if (sAuthorityUri == null) {
                sAuthorityUri = Uri.parse("content://${getAuthority()}")
            }
            return sAuthorityUri!!
        }

    }
}