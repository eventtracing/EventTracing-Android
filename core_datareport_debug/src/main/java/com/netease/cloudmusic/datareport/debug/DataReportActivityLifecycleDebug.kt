package com.netease.cloudmusic.datareport.debug

import android.app.Activity
import android.view.ViewGroup
import com.netease.cloudmusic.datareport.debug.global.DataReportDragManager
import com.netease.cloudmusic.datareport.debug.num.DebugEventNumManager
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener
import java.util.*

object DataReportActivityLifecycleDebug : DefaultEventListener() {

    private val numberMap = WeakHashMap<Activity, DebugEventNumManager>()
    private var isFirst = false

    override fun onActivityDestroyed(activity: Activity?) {
        super.onActivityDestroyed(activity)
        numberMap.remove(activity)?.onDetachedFromWindow()
    }

    override fun onActivityCreate(activity: Activity?) {
        if (!DataReportInner.getInstance().configuration.isDebugUIEnable) {
            return
        }
        if (!isFirst) {
            isFirst = true
            DataReportDragManager
        }

        (activity?.window?.decorView as? ViewGroup?)?.let {
            val numManager = DebugEventNumManager(activity)
            numberMap[activity] = numManager
        }
    }
}