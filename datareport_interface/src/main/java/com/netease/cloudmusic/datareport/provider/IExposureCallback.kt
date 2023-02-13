package com.netease.cloudmusic.datareport.provider

import android.graphics.Rect
import android.view.View

/**
 * 一个View如果执行了曝光或者反曝光，就会在执行之前先调用下面的回调来通知出去
 */
interface IExposureCallback {

    /**
     * 通知马上要进行曝光了
     * @param event 曝光的事件, _ev/_pv
     * @param view 曝光的view
     * @param visibleRect 曝光的实际可见区域
     */
    fun onExposure(event: String, oid: String, view: View?, visibleRect: Rect)

    /**
     * 通知马上要进行反曝光了
     * @param event 反曝光的事件, _ed/_pd
     * @param view 反曝光的view
     */
    fun onDisExposure(event: String, oid: String, view: View?)
}