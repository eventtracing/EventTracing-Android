package com.netease.cloudmusic.datareport.debug.ws

import com.netease.cloudmusic.datareport.debug.DataReportDebugInstaller

class StatisticConst {
    companion object{
        const val SPLIT = '\u0001'
        const val SEP = "_"
        const val EXPIRED_TIME = (24 * 3600 * 1000).toLong()
        const val MAX_UPLOAD_FILES_COUNT = 5
        const val VIEWER_ROUTER_HOST = "devtool"
        const val VIEWER_ROUTER_PATH = "bilog_viewer/connect"
        val BROADCAST_ACTIONS_SET_VIEWER_URL: String =
            DataReportDebugInstaller.mContext?.packageName + ".action.SET_VIEWER_URL"
        const val EXTRA_VIEWER_URL = "viewer_url"
    }
}