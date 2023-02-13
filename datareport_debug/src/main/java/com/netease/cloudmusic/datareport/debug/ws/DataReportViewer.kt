package com.netease.cloudmusic.datareport.debug.ws

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.netease.cloudmusic.datareport.debug.DataReportDebugInstaller
import com.netease.cloudmusic.datareport.debug.ws.StatisticConst.Companion.BROADCAST_ACTIONS_SET_VIEWER_URL
import com.netease.cloudmusic.datareport.debug.ws.StatisticConst.Companion.EXTRA_VIEWER_URL
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object DataReportViewer {

    private val taskExecutor: ExecutorService =
        ThreadPoolExecutor(4, 2000, 60L, TimeUnit.MILLISECONDS, SynchronousQueue<Runnable>())

    private val viewerAgent = StatisticViewerAgent()
    private val atomicSeq = AtomicLong(0)

    fun connectWsServer(url: String) {
        DataReportDebugInstaller.mContext?.sendBroadcast(Intent(BROADCAST_ACTIONS_SET_VIEWER_URL).apply { putExtra(EXTRA_VIEWER_URL, url) })
    }

    fun initReport(context: Context?){
        viewerAgent.initBroadcast(context)
    }

    fun uploadLog(action: String, json: JSONObject) {
        uploadLogAsync(generateLogBody(action, json))
    }

    private fun generateLogBody(action: String, logContent: JSONObject): String {
        val content = JSONObject().apply {
            put("index", atomicSeq.getAndIncrement())
            put("logTime",  System.currentTimeMillis() / 1000)
            put("content", logContent)
            put("action", action)
            put("os", "android")
            put("logtype", "ua")
            put("et", 1)
        }
        val json = JSONObject().apply {
            put("action", "log")
            put("content", content)
        }
        return json.toString()
    }

    @SuppressLint("ForbidDeprecatedUsageError", "CheckResult")
    private fun uploadLogAsync(logBody: String) {
        taskExecutor.execute {
            viewerAgent.upload(logBody)
        }
    }
}