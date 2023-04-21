package com.netease.cloudmusic.datareport.vtree.traverse

import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

interface ITraverseCallback {
    /**
     * 遍历过程中，进入到某一个节点的回调
     *
     * @param node  当前进入的视图节点
     * @param layer 当前节点所在的层次，根节点为第1层
     * @return 处理结果，通过节点返回是否要继续递归遍历
     */
    fun onEnter(node: VTreeNode, layer: Int): Boolean

    /**
     * 遍历过程中，离开某一个节点的回调
     *
     * @param node  当前进入的视图节点
     * @param layer 当前节点所在的层次
     */
    fun onLeave(node: VTreeNode, layer: Int)
}