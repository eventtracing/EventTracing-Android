package com.netease.cloudmusic.datareport.report.refer

import android.content.ContentValues
import android.content.SharedPreferences
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.provider.IProcessUpdateAction
import com.netease.cloudmusic.datareport.provider.ProcessUpdateManager
import com.netease.cloudmusic.datareport.utils.SPUtils
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 * mutable refer 的定义：
 * 把每一次根页面曝光的psRefer拼接在一起，形成一个refer的列表。这个列表就是mutable refer
 */
internal class MutableReferStorage(key: String = ""): IProcessUpdateAction {

    private val mutableReferStorage = "${key}mutable_refer_storage"

    private val mutableReferRootExposureAction = "${key}mutable_refer_root_exposure_action"
    private val mutableReferOnRootExposure = "${key}mutable_refer_on_root_exposure"

    init {
        ProcessUpdateManager.registerAction(mutableReferRootExposureAction, this)
    }

    fun onRootExposure(psRefer: String?) {
        if (psRefer == null) {
            return
        }
        SPUtils.edit().forSyncAction(mutableReferRootExposureAction).putString(mutableReferOnRootExposure, psRefer).apply()
    }

    override fun doUpdate(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor, values: ContentValues): List<String>? {
        val size = DataReportInner.getInstance().configuration.referStrategy?.mutableReferLength() ?: 0
        if (size == 0) {
            return null
        }
        val psRefer = values.getAsString(mutableReferOnRootExposure)
        try {
            if (psRefer == null) {
                return null
            }
            val referStack = JSONArray(sharedPreferences.getString(mutableReferStorage, "[]"))
            if (psRefer.startsWith("[s]")) {
                for (index in 0 until referStack.length()) {
                    referStack.remove(0)
                }
            }
            var isContains = false
            if (referStack.length() > 0) {
                for (index in referStack.length() - 1 downTo 0) {
                    if (referStack.getJSONObject(index).has(psRefer)) {
                        isContains = true
                        break
                    }
                }
            }
            if (isContains) {
                while (referStack.length() > 0 && !referStack.getJSONObject(referStack.length() - 1).has(psRefer)) {
                    referStack.remove(referStack.length() - 1)
                }
            } else {
                referStack.put(JSONObject().apply { put(psRefer, psRefer) })
                if (referStack.length() > size) {
                    referStack.remove(0)
                }
            }
            editor.putString(mutableReferStorage, referStack.toString())
            return arrayListOf(mutableReferStorage)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            values.remove(mutableReferOnRootExposure)
        }
        return null
    }

    fun getMutableReferForArray(): JSONArray {
        val size = DataReportInner.getInstance().configuration.referStrategy.mutableReferLength()
        if (size == 0) {
            return JSONArray()
        }
        return try {
            val referStack = JSONArray(SPUtils.get(mutableReferStorage, "[]"))
            val jsonArray = JSONArray()
            var index = referStack.length() - 1
            while (index >= 0 && jsonArray.length() < size) {
                val jsonObj = referStack.getJSONObject(index)
                val names = jsonObj.names()
                if(names != null){
                    jsonArray.put(jsonObj[names.getString(0)])
                }
                index--
            }
            jsonArray
        } catch (e: Exception) {
            JSONArray()
        }
    }

    fun getMutableRefer(): String {
        val size = DataReportInner.getInstance().configuration.referStrategy.mutableReferLength()
        if (size == 0) {
            return "[]"
        }
        return try {
            val referStack = JSONArray(SPUtils.get(mutableReferStorage, "[]"))
            val jsonArray = JSONArray()
            var index = referStack.length() - 1
            while (index >= 0 && jsonArray.length() < size) {
                val jsonObj = referStack.getJSONObject(index)
                val names = jsonObj.names()
                if(names != null){
                    jsonArray.put(jsonObj[names.getString(0)])
                }
                index--
            }
            return jsonArray.toString()
        } catch (e: Exception) {
            "[]"
        }
    }

    fun clear() {
        SPUtils.put(mutableReferStorage, "[]")
    }
}