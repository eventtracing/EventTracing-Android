package com.netease.cloudmusic.datareport.report.refer

import android.content.ContentValues
import android.content.SharedPreferences
import android.os.Build
import com.netease.cloudmusic.datareport.data.ReusablePool
import com.netease.cloudmusic.datareport.provider.IProcessUpdateAction
import com.netease.cloudmusic.datareport.provider.ProcessUpdateManager
import com.netease.cloudmusic.datareport.report.TO_OID
import com.netease.cloudmusic.datareport.report.UPLOAD_TIME
import com.netease.cloudmusic.datareport.report.data.FinalData
import com.netease.cloudmusic.datareport.utils.SPUtils
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 * 用一个队列来保存event埋点（click和custom），当大小超过 @param size 的时候会把最先进队列的数据抛弃掉
 * 每次进队列都会保存时间，如果通过toid没有匹配上，就会拿最新的没有超过 @param outTime 时间的数据
 */
internal class QueueList(){

    companion object {
        private const val QUEUE_SIZE = 5 //容器最大数量
        private const val EVENT_REFER_LIST_KEY = "event_refer_list_id" //refer 列表在sp中保存的key

        private const val ADD_EVENT_REFER_ACTION = "add_event_refer_action"
        private const val ON_ADD_EVENT_REFER = "on_add_event_refer"

        /**
         * 需要在冷启动的第一时间调用该方法注册action
         * 目前选择在ContentProvider初始化的时候，调用该方法
         */
        internal fun registerPreferenceAction() {
            ProcessUpdateManager.registerAction(ADD_EVENT_REFER_ACTION, eventReferAction)
        }

        private val eventReferAction = object : IProcessUpdateAction {
            override fun doUpdate(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor, values: ContentValues): List<String>? {
                try {
                    val dataStr = values.getAsString(ON_ADD_EVENT_REFER) ?: return null
                    val data = JSONObject(dataStr)

                    val jsonString = sharedPreferences.getString(EVENT_REFER_LIST_KEY, "[]") ?: "[]"
                    var jsonArray: JSONArray

                    if (Build.VERSION.SDK_INT >= 19) {
                        jsonArray = JSONArray(jsonString)
                        while (jsonArray.length() >= QUEUE_SIZE) {
                            jsonArray.remove(0)
                        }
                    } else {
                        jsonArray = JSONArray()
                        val jsonArrayTemp = JSONArray(jsonString)
                        for (i in jsonArrayTemp.length() - 1 downTo 0) {
                            jsonArray.put(jsonArrayTemp.get(i))
                            if (jsonArray.length() >= QUEUE_SIZE - 1) {
                                break
                            }
                        }
                    }

                    jsonArray.put(data)
                    editor.putString(EVENT_REFER_LIST_KEY, jsonArray.toString())
                    return arrayListOf(EVENT_REFER_LIST_KEY)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    values.remove(ON_ADD_EVENT_REFER)
                }
                return null
            }
        }
    }

    init {
        registerPreferenceAction()
    }

    fun add(data: FinalData) {
        SPUtils.edit().forSyncAction(ADD_EVENT_REFER_ACTION).putString(ON_ADD_EVENT_REFER, JSONObject(data.getEventParams()).toString()).apply()
    }

    fun getCurrentData(tOid: String): FinalData? {
        val jsonString = SPUtils.get(EVENT_REFER_LIST_KEY, "[]")
        val jsonArray = JSONArray(jsonString)
        if (jsonArray.length() == 0) {
            return null
        }
        for (index in jsonArray.length() - 1 downTo 0) {
            val item = jsonArray.getJSONObject(index)
            if (item.has(TO_OID)) {
                val oIds = item.getJSONArray(TO_OID)
                for (i in 0 until oIds.length()) {
                    if (oIds[i] == tOid) {
                        return jsonToFinalData(item)
                    }
                }
            }
        }

        val jsonObjectFinalData = jsonArray.getJSONObject(jsonArray.length() - 1)
        val time = try {
            jsonObjectFinalData.getLong(UPLOAD_TIME)
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
        if (time < ReferManager.getLastUndefineTime()) {
            return null
        }
        return jsonToFinalData(jsonObjectFinalData)
    }

    private fun jsonToFinalData(jsonObject: JSONObject): FinalData {
        val map = mutableMapOf<String, Any>()
        for (key in jsonObject.keys()) {
            map[key] = jsonObject.get(key)
        }
        return (ReusablePool.obtain(ReusablePool.TYPE_FINAL_DATA_REUSE) as FinalData).apply { this.putAll(map) }
    }

    fun clear() {
        SPUtils.put(EVENT_REFER_LIST_KEY, "[]")
    }
}