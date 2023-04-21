package com.netease.cloudmusic.datareport.provider

import com.netease.cloudmusic.datareport.IVTreeNode

/**
 * IVTreeNode级别的事件回调
 */
interface INodeEventCallback {
    /**
     * 事件回调
     * @param event 事件code
     * @param node 曝光的节点的
     */
    fun onEvent(event: String, node: IVTreeNode)
}