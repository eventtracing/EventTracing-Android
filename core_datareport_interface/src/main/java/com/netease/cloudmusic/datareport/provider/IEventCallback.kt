package com.netease.cloudmusic.datareport.provider

interface IEventCallback {

    /**
     * 事件回调
     * @param event 事件code
     * @param hashCode 曝光的节点的
     */
    fun onEvent(event: String, hashCode: Int)
}