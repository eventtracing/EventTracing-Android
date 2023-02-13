package com.netease.cloudmusic.datareport.vtree

import android.app.Activity
import android.app.Dialog
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.utils.ViewCompatUtils
import com.netease.cloudmusic.datareport.vtree.VTreeManager.getCurrentVTreeInfo
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.exposure.VTreeExposureManager
import com.netease.cloudmusic.datareport.vtree.page.DialogListUtil
import com.netease.cloudmusic.datareport.vtree.page.ViewContainerBinder
import java.util.*

/**
 * 深拷贝一棵树，并且把里面的相同节点替换成新的节点,
 * 如果需要拷贝的树是空的，返回空
 * 如果需要替换的子树是空的，返回空
 * 替换完之后，子树的虚拟节点会被回收掉，子树的treeMap会被clear掉
 * 如果在需要复制的树里没有找到子树，整个子树都被会回收，并且复制的树也会被回收，返回空
 * @param vTreeMapParam 需要拷贝的树
 * @param fixVTreeMap 拷贝的时候需要替换的子树
 */
fun copyVTreeFix(vTreeMapParam: VTreeMap?, fixVTreeMap: VTreeMap?): VTreeMap? {
    val vTreeNode = vTreeMapParam?.vTreeNode ?: return null
    if (fixVTreeMap?.vTreeNode == null || fixVTreeMap.vTreeNode.childrenList.isEmpty()) {
        return null
    }

    val newNode = vTreeNode.getDeepCopyNode()
    val treeMap = WeakHashMap<View, VTreeNode>()
    val vTreeMap = VTreeMap(newNode, treeMap, vTreeMapParam.rootPage)

    for (childNode in vTreeNode.childrenList) {
        copyTreeItemFix(childNode, newNode, fixVTreeMap, treeMap)
    }
    if (!fixVTreeMap.treeMap.isEmpty()) {
        fixVTreeMap.treeMap.clear()
        treeMap.clear()
        return null
    }
    return vTreeMap
}

private fun copyTreeItemFix(vTreeNode: VTreeNode, parentNode: VTreeNode, fixVTreeMap: VTreeMap, treeMap: WeakHashMap<View, VTreeNode>) {

    if (fixVTreeMap.treeMap.isNotEmpty() && vTreeNode.getNode() == fixVTreeMap.vTreeNode?.childrenList?.get(0)?.getNode()?:return) {
        fixVTreeMap.vTreeNode.childrenList[0].parentNode = parentNode
        parentNode.childrenList.add(fixVTreeMap.vTreeNode.childrenList[0])
        treeMap.putAll(fixVTreeMap.treeMap)
        fixVTreeMap.treeMap.clear()
        return
    }

    val newNode = vTreeNode.getDeepCopyNode()
    newNode.parentNode = parentNode
    parentNode.childrenList.add(newNode)
    newNode.getNode()?.let {
        treeMap[it] = newNode
    }
    for (childNode in vTreeNode.childrenList) {
        copyTreeItemFix(childNode, newNode, fixVTreeMap, treeMap)
    }
}

/**
 * 通过一个对象获取node
 * @param targetView 他只能是 activity，dialog或者view
 */
fun getVTreeNode(targetView: Any?): VTreeNode? {

    val view = getView(targetView)
    if (view != null && ViewCompatUtils.isAttachedToWindow(view)) {
        return findVTreeNode(view)
    }
    return null
}

/**
 * 通过一个对象获取view
 * @param targetView 他只能是 activity，dialog或者view
 */
fun getView(targetView: Any?): View?{
    return when (targetView) {
        is Activity -> {
            targetView.window?.decorView
        }
        is Dialog -> {
            targetView.window?.decorView
        }
        is View -> {
            targetView
        }
        else -> {
            null
        }
    }
}

/**
 * 查找当前视图本身或者承载视图的容器是否是一个页面
 * @return 是页面的话返回页面，否则返回null
 */
fun findRelatedPage(view: View?): Any? {
    // 先查找这个view自身有没有被设置pid
    return if (isPage(view)) {
        ViewContainerBinder.getInstance().getBoundContainer(view) ?: view
    } else {
        null
    }
}

/**
 * 寻找这个view所处的activity
 */
fun findAttachedActivity(view: View?): Activity? {
    if (view == null) {
        return null
    }
    if (!ViewCompatUtils.isAttachedToWindow(view)) {
        return null
    }
    val decorView = view.rootView
    val container = ViewContainerBinder.getInstance().getBoundContainer(decorView)
    if (container is Activity) {
        return changeTransparentActivity(container)
    }
    return if (container is Dialog) DialogListUtil.getDialogActivity(container) else AppEventReporter.getInstance().currentActivity
}

private fun findVTreeNode(targetView: View): VTreeNode? {
    return VTreeExposureManager.getVTreeCache()[targetView]
}

/**
 * 判断一个对象是否是一个页面
 */
private fun isPage(obj: Any?): Boolean {
    return !TextUtils.isEmpty(DataRWProxy.getPageId(obj))
}

/**
 * 判断该节点是否是根节点
 */
fun isRootPage(treeNode: VTreeNode): Boolean {
    if (!treeNode.isPage()) {
        return false
    }
    if(treeNode.getInnerParam(InnerKey.VIEW_AS_ROOT_PAGE) == true) {
        return true
    }
    var parent = treeNode.parentNode
    while (parent != null) {
        if (parent.isPage()) {
            return false
        }
        parent = parent.parentNode
    }
    return true
}

/**
 * 获取根节点
 */
fun getRootPageOrRootElement(treeNode: VTreeNode): VTreeNode? {
    var parent: VTreeNode? = treeNode

    var page: VTreeNode? = null
    var element: VTreeNode? = null

    while (parent != null) {
        if (parent.isPage()) {
            if(parent.getInnerParam(InnerKey.VIEW_AS_ROOT_PAGE) == true) {
                return parent
            }
            page = parent
        } else {
            element = parent
        }
        parent = parent.parentNode
    }

    return page ?: element
}

/**
 * 通过 View 获取oid
 */
fun getOid(view: View): String? {
    val oid = DataRWProxy.getPageId(view)
    if (oid != null) {
        return oid
    }
    return DataRWProxy.getElementId(view)
}

/**
 * 通过一个View向上查找父View（注意是有埋点的View）
 */
fun getOidParents(view: View?): View? {
    val tempParent = view?.parent
    var tempView = if (tempParent is View) {
        tempParent as View
    } else {
        null
    }

    while (tempView != null) {
        if (getOid(tempView) != null) {
            return tempView
        }
        val parent = tempView.parent
        tempView = if (parent is View) {
            parent
        } else {
            null
        }
    }
    return null
}

/**
 * 通过一个View 向上查找oid对应的View
 */
fun getParentByOid(view: View?, oid: String): View? {
    val tempParent = view?.parent
    var tempView = if (tempParent is View) {
        tempParent as View
    } else {
        null
    }
    while (tempView != null) {
        if (getOid(tempView) == oid) {
            return tempView
        }
        val parent = tempView.parent
        tempView = if (parent is View) {
            parent
        } else {
            null
        }
    }
    return null
}

/**
 * 通过View向下查找有oid的子View
 */
fun getOidChild(view: View): View? {
    if (view is ViewGroup) {
        val count = view.childCount
        for (index in 0 until count) {
            val indexView = view.getChildAt(index)
            if (getOid(indexView) != null) {
                return indexView
            } else {
                getOidChild(indexView)?.let { return it }
            }
        }
    }
    return null
}

/**
 * 通过View和oid 向下查找子view的oid和传入的参数相同的view
 * @param view 向下查找的View
 * @param oid 匹配的oid
 */
fun getChildByOid(view: View, oid: String): View? {
    if (view is ViewGroup) {
        val count = view.childCount
        for (index in 0 until count) {
            val indexView = view.getChildAt(index)
            if (getOid(indexView) == oid) {
                return indexView
            } else {
                getChildByOid(indexView, oid)?.let { return it }
            }
        }
    }
    return null
}

fun changeTransparentActivity(targetActivity: Activity?): Activity? {
    if (DataRWProxy.isTransparentActivity(targetActivity)) {
        return AppEventReporter.getInstance().getPreActivity(targetActivity)
    }
    return targetActivity
}

/**
 * 通过spm获取当前VTree的view
 * @param spm 需要获取的view的spm
 */
fun getViewBySpm(spm: String): View? {
    val spmSplit = spm.split("|").reversed()
    val node = getCurrentVTreeInfo()?.vTreeNode
    if (node != null && spmSplit.isNotEmpty()) {
        return getViewBySpmInner(node, spmSplit, 0)
    }
    return null
}

private fun getViewBySpmInner(node: VTreeNode, listSpm: List<String>, layer: Int): View? {
    val spmItem = listSpm[layer]
    var spm: String? = null
    var pos: Int? = null
    if (spmItem.contains(":")) {
        spmItem.split(":").let {
            spm = it[0]
            pos = try {
                it[1].toInt()
            } catch (e: Exception) {
                null
            }
        }
    } else {
        spm = spmItem
    }
    node.childrenList.forEach {
        if (it.getOid() == spm) {
            if ((pos == null && node.getPos() == null) || (pos == node.getPos())) {
                return if (layer >= listSpm.size - 1) {
                    it.getNode()
                } else {
                    getViewBySpmInner(it, listSpm, layer + 1)
                }
            }
        }
    }

    return null
}

/**
 * 获取最叶子自节点
 * @param vTreeNode 树的根节点
 */
fun getChildNode(vTreeNode: VTreeNode?): VTreeNode? {
    vTreeNode?.let {
        return getChildNodeInner(it)
    }
    return null
}

private fun getChildNodeInner(vTreeNode: VTreeNode): VTreeNode? {
    val childList = vTreeNode.childrenList
    if (childList.size > 0) {
        for (i in (childList.size - 1) downTo 0) {
            getChildNodeInner(childList[i])?.apply {
                return this
            }
        }
    }
    if (vTreeNode.isPage() && vTreeNode.getInnerParam(InnerKey.VIEW_IGNORE_CHILD_PAGE) != true) {
        return vTreeNode
    }
    return null
}