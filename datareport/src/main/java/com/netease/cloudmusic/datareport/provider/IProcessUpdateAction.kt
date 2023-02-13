package com.netease.cloudmusic.datareport.provider

import android.content.ContentValues
import android.content.SharedPreferences

interface IProcessUpdateAction {

    /**
     * @param values 这里面的key用完之后，记得要remove掉
     */
    fun doUpdate(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor, values: ContentValues): List<String>?

}