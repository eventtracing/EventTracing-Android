package com.netease.cloudmusic.datareport.report.refer

import com.netease.cloudmusic.datareport.data.ReusablePool
import com.netease.cloudmusic.datareport.data.ReusablePool.TYPE_FINAL_DATA_REUSE
import com.netease.cloudmusic.datareport.report.data.FinalData
import com.netease.cloudmusic.datareport.utils.SPUtils
import org.json.JSONObject

/**
 * refer数据保存
 * 该类中的所有方法都是在 埋点子线程中执行的
 */
internal class ReferStorage {

    companion object {

        private const val LAST_PAGE_VIEW_KEY = "last_page_view_id"
    }

    private var lastUndefineTime = 0L
    private val queueList = QueueList()

    fun updateLastPageView(pageView: FinalData) {
        SPUtils.put(LAST_PAGE_VIEW_KEY, JSONObject(pageView.getEventParams()).toString())
    }

    fun addEventUpload(eventData: FinalData) {
        queueList.add(eventData)
    }

    fun getLastEventRefer(tOid: String): FinalData? {
        return queueList.getCurrentData(tOid)
    }

    fun getLastPageView(): FinalData? {
        val jsonString = SPUtils.get(LAST_PAGE_VIEW_KEY, "")
        if (jsonString == "") {
            return null
        }
        return stringToFinalData(jsonString)
    }

    internal fun updateUndefineTime() {
        lastUndefineTime = System.currentTimeMillis()
    }

    internal fun getLastUndefineTime(): Long {
        return lastUndefineTime
    }

    private fun stringToFinalData(jsonString: String): FinalData {
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, Any>()
        for (key in jsonObject.keys()) {
            map[key] = jsonObject.get(key)
        }

        return (ReusablePool.obtain(TYPE_FINAL_DATA_REUSE) as FinalData).apply { this.putAll(map) }
    }

    fun clearData() {
        SPUtils.put(LAST_PAGE_VIEW_KEY, "")
        queueList.clear()
    }

}