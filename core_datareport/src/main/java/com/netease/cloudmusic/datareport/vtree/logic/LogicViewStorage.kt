package com.netease.cloudmusic.datareport.vtree.logic

import android.app.Activity
import android.view.View
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.InnerKey
import java.lang.ref.WeakReference
import java.util.*

class LogicViewStorage {

    private val mActivityViewMap = WeakHashMap<Activity, MutableList<WeakReference<View?>>>()

    /**
     * 设置root节点抢占的view
     */
    private val mActivityRootMap = WeakHashMap<Activity, WeakReference<View?>>()

    fun setRootView(activity: Activity, rootView: View) {
        mActivityRootMap[activity] = WeakReference(rootView)
    }

    fun getRootView(activity: Activity?): View? {
        if (activity == null) {
            return null
        }
        return mActivityRootMap[activity]?.get()
    }

    fun addAlertView(activity: Activity, alertView: View) {
        var list = mActivityViewMap[activity]
        if (list == null) {
            list = mutableListOf()
            mActivityViewMap[activity] = list
        }
        list.forEach {
            if (it.get() == alertView) {
                return
            }
        }
        list.add(WeakReference(alertView))
    }

    fun deleteAlertView(activity: Activity, alertView: View) {
        val list = mActivityViewMap[activity]
        if (list != null) {
            var index = 0
            while (index < list.size) {
                val view = list[index].get()
                if (view != null && view === alertView) {
                    break
                }
                index++
            }
            if (index < list.size) {
                list.removeAt(index)
            }
        }
    }

    fun getAlertViewList(activity: Activity?): MutableList<WeakReference<View?>> {
        if (activity == null) {
            return mutableListOf()
        }
        var list = mActivityViewMap[activity]
        if (list == null) {
            list = ArrayList()
        }
        list.sortBy { (DataRWProxy.getInnerParam(it.get(), InnerKey.VIEW_ALERT_PRIORITY) as? Int?) ?: 0 }
        return list
    }

    fun onActivityDestroy(activity: Activity?) {
        mActivityViewMap.remove(activity)
        mActivityRootMap.remove(activity)
    }
}