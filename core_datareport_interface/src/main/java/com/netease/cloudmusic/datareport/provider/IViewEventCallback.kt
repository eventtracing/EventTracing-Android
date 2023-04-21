package com.netease.cloudmusic.datareport.provider

import android.view.View

interface IViewEventCallback {

    /**
     * 事件回调
     * @param event 事件code
     * @param view 曝光的节点的
     */
    fun onEvent(event: String, view: View?)
}