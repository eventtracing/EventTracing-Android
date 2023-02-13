package com.netease.cloudmusic.datareport.debug

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.netease.cloudmusic.datareport.debug.ws.DataReportViewer
import com.netease.cloudmusic.datareport.inject.EventCollector

/**
 * 初始化的地方，通过ContentProvider初始化，业务无感知
 */
class DataReportDebugInstaller : ContentProvider() {

    companion object{
        var mContext: Context? = null
    }

    override fun onCreate(): Boolean {
        mContext = this.context?.applicationContext
        EventCollector.getInstance().registerEventListener(DataReportActivityLifecycleDebug)
        DataReportViewer.initReport(mContext)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

}