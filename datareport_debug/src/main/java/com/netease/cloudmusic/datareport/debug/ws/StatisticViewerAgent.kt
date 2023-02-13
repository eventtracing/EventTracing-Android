package com.netease.cloudmusic.datareport.debug.ws

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import com.netease.cloudmusic.datareport.debug.ws.StatisticConst.Companion.BROADCAST_ACTIONS_SET_VIEWER_URL
import com.netease.cloudmusic.datareport.debug.ws.StatisticConst.Companion.EXTRA_VIEWER_URL
import com.netease.cloudmusic.datareport.utils.ReportUtils
import okhttp3.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 埋点平台日志实时查看工具的日志上报的客户端
 */
class StatisticViewerAgent {
    companion object {
        private const val CODE_CHANGE_URL = 1000
        private const val STATUS_INIT = 0
        private const val STATUS_OPENING = 1
        private const val STATUS_OPENED = 2
        private const val STATUS_CLOSED = 3
        private const val STATUS_FAILED = 4
    }
    @Volatile
    private var url: String = ""
    @Volatile
    private var curWebSocket: WebSocket? = null
    @Volatile
    private var status = STATUS_CLOSED
    private val lock = ReentrantLock()
    private val okHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }
    private val msgList = mutableListOf<String>()
    private val maxMsgSize = 500

    fun initBroadcast(context: Context?){
        //考虑到多进程，需要在每一个进程都进行连接，这里使用广播
        context?.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    updateUrl(intent.getStringExtra(EXTRA_VIEWER_URL))
                }
            }
        }, IntentFilter(BROADCAST_ACTIONS_SET_VIEWER_URL))
    }

    private fun updateUrl(url: String?) {
        if (url.isNullOrEmpty()) {
            return
        }
        if (this.url != url) {
            this.url = url
            toggleClient(url)
        }
    }

    private fun toggleClient(url: String) {
        val oldWebSocket = curWebSocket
        val wsUrl = Uri.parse(url).buildUpon().appendPath("0").build().toString()
        val request = Request.Builder().url(wsUrl).build()
        lock.withLock {
            if (status == STATUS_OPENING) {
                return
            }
            status = STATUS_INIT
        }
        curWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (webSocket == curWebSocket) {
                    lock.withLock {
                        status = STATUS_OPENED
                        sendMessageList(webSocket)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (webSocket == curWebSocket) {
                    lock.withLock {
                        status = STATUS_CLOSED
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (webSocket == curWebSocket) {
                    lock.withLock {
                        status = STATUS_FAILED
                    }
                }
            }
        })
        lock.withLock {
            status = STATUS_OPENING
        }
        disconnect(oldWebSocket, CODE_CHANGE_URL)
    }

    fun upload(msg: String) {
        lock.withLock {
            when {
                isNormalStatus() -> {
                    curWebSocket?.send(msg)
                }
                status == STATUS_FAILED -> { //失败后重新尝试创建ws连接
                    toggleClient(url)
                    curWebSocket?.send(msg)
                }
                else -> {
                    addMessageList(msg)
                    false
                }
            }
        }
    }

    private fun addMessageList(msg: String) {
        lock.withLock {
            while (msgList.size > maxMsgSize) {
                msgList.removeAt(0)
            }
            msgList.add(msg)
        }
    }

    private fun sendMessageList(socket: WebSocket) {
        lock.withLock {
            msgList.forEach {
                socket.send(it)
            }
            msgList.clear()
        }
    }

    private fun isNormalStatus(): Boolean {
        return status == STATUS_INIT || status == STATUS_OPENED
    }

    private fun disconnect(oldWebSocket: WebSocket?, code: Int) {
        oldWebSocket?.close(code, null)
    }
}