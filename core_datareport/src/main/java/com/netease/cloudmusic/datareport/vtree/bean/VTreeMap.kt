package com.netease.cloudmusic.datareport.vtree.bean

import android.view.View
import java.util.*

/**
 * 构建生成的一个虚拟数的基本信息
 * @param vTreeNode 虚拟树的根节点上面的虚拟节点信息
 * @param treeMap 全部节点的map
 * @param rootPage 根节点
 */
data class VTreeMap(val vTreeNode: VTreeNode?, val treeMap: WeakHashMap<View, VTreeNode>, val rootPage: VTreeNode?)