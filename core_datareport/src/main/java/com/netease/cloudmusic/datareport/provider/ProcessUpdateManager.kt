package com.netease.cloudmusic.datareport.provider

import android.content.ContentValues
import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

/**
 * 主要用来解决 sp的一次读写是顺序执行的，中间不会插入其他的读和写的操作
 */
object ProcessUpdateManager {
    private val actionList = ConcurrentHashMap<String, IProcessUpdateAction>()

    fun registerAction(key: String, action: IProcessUpdateAction) {
        if (!actionList.contains(key)) {
            actionList[key] = action
        }
    }

    fun unRegisterAction(key: String) {
        actionList.remove(key)
    }

    fun invokeAction(key: String, sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor, values: ContentValues?): List<String>? {
        return values?.let {
            actionList[key]?.doUpdate(sharedPreferences, editor, values)
        }
    }

}