package com.netease.cloudmusic.datareport.vtree.traverse

import android.view.View
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

/**
 * 定义一个用来生成VTree以及遍历VTree某个节点的接口
 */
interface IVTreeTraverser {

    /**
     * 生成一颗虚拟VTree，会一边生成VTree，一边遍历然后调用回调 callback 输出
     * @param view 一个节点，不一定是根节点，最终都会先获取他的根节点
     * @param listDialogView 在目前activity上面展示的对话框列表
     * @param callback 在生成树的过程中也会进行遍历回调的，回调接口，进入和离开节点时会通过本接口回调出去
     */
    fun buildViewTree(view: View, listDialogView: List<View>?, callback: IVTreeTraverseCallback): VTreeMap?

    /**
     * 生成一颗局部的虚拟VTree
     * @param view 一个节点，不一定是根节点，最终都会先获取他的根节点
     * @param callback 在生成树的过程中也会进行遍历回调的，回调接口，进入和离开节点时会通过本接口回调出去
     */
    fun buildPartialViewTree(view: View, callback: IVTreeTraverseCallback): VTreeMap?


    /**
     * 开始遍历某个节点
     *
     * @param entry    根节点
     * @param order 从左往右还是从右往左
     * @param callback 回调接口，进入和离开节点时会通过本接口回调出去
     */
    fun traverse(entry: VTreeNode, order: Boolean, callback: ITraverseCallback)

}