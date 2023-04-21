package com.netease.cloudmusic.datareport.vtree.traverse

import android.app.Activity
import android.app.Dialog
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.netease.cloudmusic.datareport.R
import com.netease.cloudmusic.datareport.data.DataEntity
import com.netease.cloudmusic.datareport.data.ReusablePool
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.utils.ViewCompatUtils
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.logic.LogicViewManager
import com.netease.cloudmusic.datareport.vtree.page.DialogListUtil
import com.netease.cloudmusic.datareport.vtree.page.ViewContainerBinder
import java.lang.ref.WeakReference
import java.util.*

/**
 * 提供生成虚拟VTree的能力，并且提供深度优先遍历某个VTree节点的能力
 */
object VTreeTraverser: IVTreeTraverser {

    private var mOnTraverseListener: OnViewTraverseListener? = null

    private var firstPageFind = false
    private var tempRootPage: VTreeNode? = null

    fun setListener(listener: OnViewTraverseListener?) {
        mOnTraverseListener = listener
    }

    override fun buildViewTree(view: View, listDialogView: List<View>?, callback: IVTreeTraverseCallback): VTreeMap? {
        firstPageFind = false
        tempRootPage = null

        //虚拟树的根节点，是一个虚拟节点，没有View
        val virtualNode = ReusablePool.obtainVTreeNode(null, false, false)
        val nodeMap = WeakHashMap<View, VTreeNode>()
        val dataEntity = view.getTag(R.id.key_data_package) as? DataEntity?

        performInternal(view, virtualNode, 1, nodeMap, callback, dataEntity, false)

        //虚拟父节点的获取
        getRootViewPage(view)?.let {
            nodeMap[it]?.let { node ->
                tempRootPage = node
            }
        }

        if (tempRootPage == null) {
            return null
        } else {
            val alertList = getAlertViewList(view)
            for (element in alertList) {
                val childEntity = element.getTag(R.id.key_data_package) as? DataEntity?
                val refer = (childEntity?.innerParams?.get(InnerKey.LOGIC_PARENT) as? WeakReference<*>?)?.get() as? View?
                if (refer == null) {
                    if (ViewCompatUtils.isAttachedToWindow(element)) {
                        logicMountPerformInternal(element, tempRootPage!!, 1, nodeMap, callback, element.getTag(R.id.key_data_package) as? DataEntity?, true)
                    }
                }
            }
            listDialogView?.let {
                for(element in it){
                    val childEntity = element.getTag(R.id.key_data_package) as? DataEntity?
                    val refer = (childEntity?.innerParams?.get(InnerKey.LOGIC_PARENT) as? WeakReference<*>?)?.get() as? View?
                    if (refer == null) {
                        logicMountPerformInternal(element, tempRootPage!!, 1, nodeMap, callback, element.getTag(R.id.key_data_package) as? DataEntity?, true)
                    }
                }
            }
        }
        val innerRootPage = tempRootPage
        tempRootPage = null
        return VTreeMap(virtualNode, nodeMap, innerRootPage)
    }

    override fun buildPartialViewTree(view: View, callback: IVTreeTraverseCallback): VTreeMap? {
        val dataEntity = view.getTag(R.id.key_data_package) as? DataEntity?
        val nodeMap = WeakHashMap<View, VTreeNode>()
        //虚拟树的根节点，是一个虚拟节点，没有View
        val virtualNode = ReusablePool.obtainVTreeNode(null, false, false)

        performInternalPartial(view, virtualNode, 1, nodeMap, callback, dataEntity, dataEntity?.innerParams?.get(InnerKey.LOGIC_PARENT) != null)
        return VTreeMap(virtualNode, nodeMap, null)
    }

    private fun performInternal(nodeView: View, parentNode: VTreeNode, layer: Int, nodeMap: WeakHashMap<View, VTreeNode>, callback: IVTreeTraverseCallback, dataEntity: DataEntity?, isLogic: Boolean) {
        mOnTraverseListener?.onViewVisited(nodeView)
        val tempRect = Rect()
        val actualTempRect = Rect()
        val goNext = callback.onEnter(nodeView, layer, tempRect, actualTempRect, dataEntity, isLogic)
        if (tempRect.left == 0 && tempRect.right == 0 && tempRect.top == 0 && tempRect.bottom == 0) {
            return
        }
        val id = dataEntity?.elementId ?: dataEntity?.pageId
        val entry = if(id == null) null else VTreeNode(nodeView, isVisible = true, visibleRect = tempRect).apply {
            setData(id, dataEntity?.elementId == null, dataEntity)
            this.actualRect.set(actualTempRect)
            dataEntity?.dynamicParams?.get()?.viewDynamicParams?.let {
                for (entry in it.entries) {
                    dataEntity.customParams[entry.key ?: ""] = entry.value ?: ""
                }
            }
            setInnerParams(dataEntity?.innerParams)
            val viewParamNode = this.getInnerParam(InnerKey.VIEW_VIRTUAL_PARENT_NODE)
            if (viewParamNode is VTreeNode) { //判断节点是否需要挂载到虚拟节点上去
                val virtualNode = viewParamNode.deepVirtualClone()
                virtualNode.parentNode = parentNode //必须要先做这件事，否则没有办法比较想等
                if (parentNode.childrenList.contains(virtualNode)) { //当前层级已经有一个对应的虚拟节点了
                    val virtualParent = parentNode.childrenList[parentNode.childrenList.indexOf(virtualNode)]
                    virtualParent.addParams(virtualNode.getParams()?: mapOf())
                    this.parentNode = virtualParent
                    virtualParent.childrenList.add(0, this)
                } else { //还没有虚拟节点，需要创建
                    parentNode.childrenList.add(0, virtualNode)
                    virtualNode.childrenList.add(0, this)
                    this.parentNode = virtualNode
                }
            } else {
                this.parentNode = parentNode
                parentNode.childrenList.add(0, this)
            }

//            if (nodeMap.containsValue(this)) {//便是node的值有重复
//                ExceptionReporter.reportError(NodeNotUniqueError(this))
//            }
            nodeMap[nodeView] = this
        }
        val listRef = dataEntity?.innerParams?.get(InnerKey.LOGIC_CHILDREN) as? List<WeakReference<View?>>?
        val logicChildCount = listRef?.size ?: 0
        if (goNext || logicChildCount > 0) {
            if (!firstPageFind && entry?.isPage() == true && dataEntity?.innerParams?.get(InnerKey.VIEW_ALERT_FLAG) != true) {
                firstPageFind = true
                tempRootPage = entry
            }
            //先遍历逻辑挂靠
            if (logicChildCount > 0) {
                for (i in logicChildCount - 1 downTo 0) {
                    listRef?.get(i)?.get()?.let {
                        if (ViewCompatUtils.isAttachedToWindow(it)) {
                            performInternal(it, entry
                                    ?: parentNode, layer + 1, nodeMap, callback, it.getTag(R.id.key_data_package) as? DataEntity?, true)
                        }
                    }
                }
            }

            //后遍历正常挂靠
            val childCount = (nodeView as? ViewGroup)?.childCount ?: 0
            if (childCount > 0) {
                for (i in childCount - 1 downTo 0) {
                    val childView = (nodeView as ViewGroup).getChildAt(i)
                    val childEntity = childView.getTag(R.id.key_data_package) as? DataEntity?
                    val refer = (childEntity?.innerParams?.get(InnerKey.LOGIC_PARENT) as? WeakReference<*>?)?.get() as? View?
                    if (refer == null && childEntity?.innerParams?.get(InnerKey.VIEW_ALERT_FLAG) != true) {
                        performInternal(childView, entry
                                ?: parentNode, layer + 1, nodeMap, callback, childEntity, false)
                    }
                }
            }
        }
        callback.onLeave(nodeView, layer)
    }

    private fun performInternalPartial(nodeView: View, parentNode: VTreeNode, layer: Int, nodeMap: WeakHashMap<View, VTreeNode>, callback: IVTreeTraverseCallback, dataEntity: DataEntity?, isLogic: Boolean) {
        val tempRect = Rect()
        val actualTempRect = Rect()
        val goNext = callback.onEnter(nodeView, layer, tempRect, actualTempRect, dataEntity, isLogic)
        if (tempRect.left == 0 && tempRect.right == 0 && tempRect.top == 0 && tempRect.bottom == 0) {
            return
        }
        val id = dataEntity?.elementId ?: dataEntity?.pageId
        val entry = if(id == null) null else VTreeNode(nodeView, isVisible = true, visibleRect = tempRect).apply {
            setData(id, dataEntity?.elementId == null, dataEntity)
            actualRect.set(actualTempRect)
            dataEntity?.dynamicParams?.get()?.viewDynamicParams?.let {
                for (entry in it.entries) {
                    dataEntity.customParams[entry.key ?: ""] = entry.value ?: ""
                }
            }
            setInnerParams(dataEntity?.innerParams)
            val viewParamNode = this.getInnerParam(InnerKey.VIEW_VIRTUAL_PARENT_NODE)
            if (viewParamNode is VTreeNode) { //判断节点是否需要挂载到虚拟节点上去
                val virtualNode = viewParamNode.deepVirtualClone()
                virtualNode.parentNode = parentNode //必须要先做这件事，否则没有办法比较想等
                if (parentNode.childrenList.contains(virtualNode)) { //当前层级已经有一个对应的虚拟节点了
                    val virtualParent = parentNode.childrenList[parentNode.childrenList.indexOf(virtualNode)]
                    virtualParent.addParams(virtualNode.getParams()?: mapOf())
                    this.parentNode = virtualParent
                    virtualParent.childrenList.add(this)
                } else { //还没有虚拟节点，需要创建
                    parentNode.childrenList.add(virtualNode)
                    virtualNode.childrenList.add(this)
                    this.parentNode = virtualNode
                }
            } else {
                this.parentNode = parentNode
                parentNode.childrenList.add(this)
            }

//            if (nodeMap.containsValue(this)) {//便是node的值有重复
//                ExceptionReporter.reportError(NodeNotUniqueError(this))
//            }
            nodeMap[nodeView] = this
        }
        val listRef = dataEntity?.innerParams?.get(InnerKey.LOGIC_CHILDREN) as? List<WeakReference<View?>>?
        val logicChildCount = listRef?.size ?: 0
        if (goNext || logicChildCount > 0) {
            //先遍历正常挂靠
            val childCount = (nodeView as? ViewGroup)?.childCount ?: 0
            if (childCount > 0) {
                for (i in 0 until childCount) {
                    val childView = (nodeView as ViewGroup).getChildAt(i)
                    val childEntity = childView.getTag(R.id.key_data_package) as? DataEntity?
                    val refer = (childEntity?.innerParams?.get(InnerKey.LOGIC_PARENT) as? WeakReference<*>?)?.get() as? View?
                    if (refer == null && childEntity?.innerParams?.get(InnerKey.VIEW_ALERT_FLAG) != true) {
                        performInternalPartial(childView, entry?: parentNode, layer + 1, nodeMap, callback, childEntity, false)
                    }
                }
            }

            //再遍历逻辑挂靠
            if (logicChildCount > 0) {
                for (i in 0 until logicChildCount) {
                    listRef?.get(i)?.get()?.let {
                        if (ViewCompatUtils.isAttachedToWindow(it)) {
                            performInternalPartial(it, entry?: parentNode, layer + 1, nodeMap, callback, it.getTag(R.id.key_data_package) as? DataEntity?, true)
                        }
                    }
                }
            }
        }
        callback.onLeave(nodeView, layer)
    }

    private fun getActivity(nodeView: View): Activity? {
        val container = ViewContainerBinder.getInstance().getBoundContainer(nodeView.rootView)
        return if (container is Activity) {
            container
        } else {
            if (container is Dialog) DialogListUtil.getDialogActivity(container) else null
        }
    }

    private fun getRootViewPage(nodeView: View): View? {
        return LogicViewManager.getRootPage(getActivity(nodeView))
    }

    private fun getAlertViewList(nodeView: View): List<View>{
        return LogicViewManager.getAlertViewList(getActivity(nodeView)).mapNotNull { it.get() }
    }

    override fun traverse(entry: VTreeNode, order: Boolean, callback: ITraverseCallback) {
        traverseInternal(entry, order, 1, callback)
    }

    private fun traverseInternal(entry: VTreeNode, order: Boolean, layer: Int, callback: ITraverseCallback) {

        // 回调进入entry的事件

        val node = entry.getNode()
        var flag = true
        if (node != null || entry.isVirtualNode()) {
            flag = callback.onEnter(entry, layer)
        }

        if (flag) {

            if (order) {
                for (child in entry.childrenList) {
                    traverseInternal(child, order, layer + 1, callback)
                }
            } else {
                for (index in entry.childrenList.size - 1 downTo 0) {
                    traverseInternal(entry.childrenList[index], order, layer + 1, callback)
                }
            }
        }

        if (node != null || entry.isVirtualNode()) {
            callback.onLeave(entry, layer)
        }
    }

    /**
     * 为了逻辑挂在又搞了一个方法，和上面的 performInternal 相同，只有添加和遍历的顺序相反
     * 和 performInternalPartial 也相同，只是多了一个 mOnTraverseListener?.onViewVisited(nodeView) 的调用
     * 这三个方法可以抽一下，不过复杂度较高。后续再优化
     */
    private fun logicMountPerformInternal(nodeView: View, parentNode: VTreeNode, layer: Int, nodeMap: WeakHashMap<View, VTreeNode>, callback: IVTreeTraverseCallback, dataEntity: DataEntity?, isLogic: Boolean) {
        mOnTraverseListener?.onViewVisited(nodeView)
        val tempRect = Rect()
        val actualTempRect = Rect()
        val goNext = callback.onEnter(nodeView, layer, tempRect, actualTempRect, dataEntity, isLogic)
        if (tempRect.left == 0 && tempRect.right == 0 && tempRect.top == 0 && tempRect.bottom == 0) {
            return
        }
        val id = dataEntity?.elementId ?: dataEntity?.pageId
        val entry = if(id == null) null else VTreeNode(nodeView, isVisible = true, visibleRect = tempRect).apply {
            setData(id, dataEntity?.elementId == null, dataEntity)
            actualRect.set(actualTempRect)
            dataEntity?.dynamicParams?.get()?.viewDynamicParams?.let {
                for (entry in it.entries) {
                    dataEntity.customParams[entry.key ?: ""] = entry.value ?: ""
                }
            }
            setInnerParams(dataEntity?.innerParams)
            val viewParamNode = this.getInnerParam(InnerKey.VIEW_VIRTUAL_PARENT_NODE)
            if (viewParamNode is VTreeNode) { //判断节点是否需要挂载到虚拟节点上去
                val virtualNode = viewParamNode.deepVirtualClone()
                virtualNode.parentNode = parentNode //必须要先做这件事，否则没有办法比较想等
                if (parentNode.childrenList.contains(virtualNode)) { //当前层级已经有一个对应的虚拟节点了
                    val virtualParent = parentNode.childrenList[parentNode.childrenList.indexOf(virtualNode)]
                    virtualParent.addParams(virtualNode.getParams()?: mapOf())
                    this.parentNode = virtualParent
                    virtualParent.childrenList.add(this)
                } else { //还没有虚拟节点，需要创建
                    parentNode.childrenList.add(virtualNode)
                    virtualNode.childrenList.add(this)
                    this.parentNode = virtualNode
                }
            } else {
                this.parentNode = parentNode
                parentNode.childrenList.add(this)
            }

//            if (nodeMap.containsValue(this)) {//便是node的值有重复
//                ExceptionReporter.reportError(NodeNotUniqueError(this))
//            }
            nodeMap[nodeView] = this
        }

        val listRef = dataEntity?.innerParams?.get(InnerKey.LOGIC_CHILDREN) as? List<WeakReference<View?>>?
        val logicChildCount = listRef?.size ?: 0
        if (goNext || logicChildCount > 0) {
            //先遍历正常挂靠
            val childCount = (nodeView as? ViewGroup)?.childCount ?: 0
            if (childCount > 0) {
                for (i in 0 until childCount) {
                    val childView = (nodeView as ViewGroup).getChildAt(i)
                    val childEntity = childView.getTag(R.id.key_data_package) as? DataEntity?
                    val refer = (childEntity?.innerParams?.get(InnerKey.LOGIC_PARENT) as? WeakReference<*>?)?.get() as? View?
                    if (refer == null && childEntity?.innerParams?.get(InnerKey.VIEW_ALERT_FLAG) != true) {
                        logicMountPerformInternal(childView, entry?: parentNode, layer + 1, nodeMap, callback, childEntity, false)
                    }
                }
            }

            //再遍历逻辑挂靠
            if (logicChildCount > 0) {
                for (i in 0 until logicChildCount) {
                    listRef?.get(i)?.get()?.let {
                        if (ViewCompatUtils.isAttachedToWindow(it)) {
                            logicMountPerformInternal(it, entry?: parentNode, layer + 1, nodeMap, callback, it.getTag(R.id.key_data_package) as? DataEntity?, true)
                        }
                    }
                }
            }
        }
        callback.onLeave(nodeView, layer)
    }
}