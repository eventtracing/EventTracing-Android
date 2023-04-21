package com.netease.cloudmusic.datareport.vtree.exposure

import android.graphics.Rect
import android.view.View
import com.netease.cloudmusic.datareport.event.EventDispatch
import com.netease.cloudmusic.datareport.report.data.PageStepManager
import com.netease.cloudmusic.datareport.report.exception.ExceptionReporter
import com.netease.cloudmusic.datareport.report.exception.NodeSPMNotUniqueError
import com.netease.cloudmusic.datareport.utils.*
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.getChildNode
import com.netease.cloudmusic.datareport.vtree.traverse.ITraverseCallback
import com.netease.cloudmusic.datareport.vtree.traverse.VTreeTraverser
import java.util.*

/**
 * VTree发生改变之后，通过这个管理类 计算遮挡，然后处理曝光和反曝光
 */
object VTreeExposureManager {

    private const val TAG = "ExposureManager"

    private var currentExposureVTree: VTreeNode? = null

    private var mapNode = WeakHashMap<View, VTreeNode>()

    private val mListenerMgr = ListenerMgr<IExposureListener>()

    private const val CURRENT_PROCESS_OID = "current_foreground_oid"
    private const val CURRENT_PROCESS_SPM = "current_foreground_spm"

    fun registerListener(listener: IExposureListener) {
        mListenerMgr.register(listener)
    }

    fun unRegisterListener(listener: IExposureListener) {
        mListenerMgr.unregister(listener)
    }

    fun getVTreeCache(): WeakHashMap<View, VTreeNode> {
        return mapNode
    }

    fun onVTreeChange(map: VTreeMap?) {
        handleOcclude(map?.vTreeNode)
        handleExposure(map?.vTreeNode, currentExposureVTree)
        currentExposureVTree = map?.vTreeNode
        mapNode = map?.treeMap ?: WeakHashMap<View, VTreeNode>()
        updateChildPage()
    }

    /**
     * 处理VTree中所有的遮挡，会把VTree中一些看不见的节点进行修剪
     */
    private fun handleOcclude(vTreeNode: VTreeNode?) {
        vTreeNode?.let { changeVTreeBy(it) }
    }

    /**
     * 1. 深度先序遍历所有的node，如果遇到elemennt就忽略掉
     * 2. 否则就是把自己和父节点下位于自己左边的所有子节点比较
     * 3. 如果父节点是element还要继续往上找父节点，再走第2步，直到找到的父节点不是element为止
     */
    private fun changeVTreeBy(vTreeNode: VTreeNode) {
        if (vTreeNode.parentNode != null && !vTreeNode.isVisible) { //非顶层虚拟节点如果不可见，就不再往下遍历
            return
        }
        if (vTreeNode.isPage()) {
            var tempChild = vTreeNode
            var parentNode = tempChild.parentNode
            while (parentNode != null && !parentNode.isPage()) {
                val index = parentNode.childrenList.indexOf(tempChild)
                for (i in index - 1 downTo 0) {
                    val bottom = parentNode.childrenList[i]
                    if (!bottom.isVisible) {
                        continue
                    }
                    compare(vTreeNode, bottom)
                }
                tempChild = parentNode
                parentNode = tempChild.parentNode
            }
            parentNode?.let {
                val index = it.childrenList.indexOf(tempChild)
                for (i in index - 1 downTo 0) {
                    val bottom = it.childrenList[i]
                    if (!bottom.isVisible) {
                        continue
                    }
                    compare(vTreeNode, bottom)
                }
            }
        }

        for (child in vTreeNode.childrenList) {
            if (!child.isVisible) {
                continue
            }
            changeVTreeBy(child)
        }
    }

    /**
     * 拿一个node和一颗树下面的所有node比较是否遮挡
     * 如果是不可见的就清除node的子节点，并把自己标记为不可见
     */
    private val helperRect = Rect()
    private fun compare(top: VTreeNode, bottom: VTreeNode) {

        if (bottom.isVirtualNode()) { //如果是虚拟节点的话，就不比较了，直接比较子节点
            for (i in bottom.childrenList.size - 1 downTo 0) {
                compare(top, bottom.childrenList[i])
            }
            return
        }

        val topRect = top.visibleRect
        helperRect.set(bottom.visibleRect)
        if (!helperRect.intersect(topRect)) {
            return
        } else if (helperRect == bottom.visibleRect) {
            bottom.isVisible = false
            dealWithVirtualParent(bottom)
            return
        } else {
            val actualArea = bottom.visibleRect.width() * bottom.visibleRect.height()
            val exposureArea = actualArea - helperRect.width() * helperRect.height()
            bottom.exposureArea = exposureArea
            //这里把曝光比例的逻辑干掉了
            /*val rate = bottom.getInnerParam(InnerKey.VIEW_EXPOSURE_MIN_RATE)
            // 这个if 里面做的是遮挡之后的 曝光比例的大小检查
            if (rate is Float) {
                val exposureRate = exposureArea * 1.0f / actualArea
                if (exposureRate < rate) {
                    bottom.isVisible = false
                    dealWithVirtualParent(bottom)
                    return
                }
            }*/
            for (i in bottom.childrenList.size - 1 downTo 0) {
                compare(top, bottom.childrenList[i])
            }
        }
    }

    /**
     * 处理自己的父节点是虚拟节点的情况
     * 如果自己的父节点是虚拟节点，就判断父节点下面的所有节点是否全部不可见，如果全部不可见，那么虚拟父节点不可见
     */
    private fun dealWithVirtualParent(bottom: VTreeNode) {
        if (bottom.parentNode?.isVirtualNode() == true) {
            var visible = false
            bottom.parentNode?.childrenList?.forEach {
                visible = (visible || it.isVisible)
            }
            if (!visible) {
                bottom.parentNode?.isVisible = false
            }
        }
    }

    /**
     * 处理曝光
     */
    private val pgStepTemp = PgStepTemp() //帮助类，只用生成这一个就够了
    private fun handleExposure(vTreeNode: VTreeNode?, tempLastVTreeNode: VTreeNode?) {

        val tempMap = HashMap<VTreeNode, Array<Boolean>>()
        val areaMap = HashMap<VTreeNode, Float>()

        if (vTreeNode != null) {
            traverseInternal(vTreeNode, true, 1, MarkCallback(areaMap, tempMap, 1))
        }

        if (tempLastVTreeNode != null) {
            VTreeTraverser.traverse(tempLastVTreeNode, false, object :
                    ITraverseCallback {
                override fun onEnter(node: VTreeNode, layer: Int): Boolean {
                    if (!node.isVisible) {
                        return false
                    }
                    return true
                }

                override fun onLeave(node: VTreeNode, layer: Int) {

                    if (!node.isVisible) {
                        return
                    }

                    var pairFlag = tempMap[node]
                    if (pairFlag == null) {
                        pairFlag = arrayOf(false, false)
                        tempMap[node] = pairFlag
                    }
                    pairFlag[0] = true

                    tempMap[node]?.let {
                        if (!it[1]) {
                            preDisExposure(node)
                        }
                    }
                    areaMap[node] = Math.max(areaMap[node] ?: 0F, node.getExposureRate())
                }
            })
        }
        if (vTreeNode != null) {
            val currentPageStep = PageStepManager.getCurrentPageStep()
            pgStepTemp.pgStep = currentPageStep

            VTreeTraverser.traverse(vTreeNode, true, object :
                    ITraverseCallback {
                override fun onEnter(node: VTreeNode, layer: Int): Boolean {
                    tempMap[node]?.let {
                        if (!it[0] && it[1]) {
                            preExposure(node, pgStepTemp)
                        } else {
                            node.setExposureRate(areaMap[node]?:1.0f)
                        }
                    }
                    return true
                }

                override fun onLeave(node: VTreeNode, layer: Int) {
                }
            })
            if (currentPageStep != pgStepTemp.pgStep) {
                PageStepManager.setCurrentPageStep(pgStepTemp.pgStep)
            }
        }
    }

    class PgStepTemp {
        var pgStep: Int = 0
    }

    private fun preExposure(vTreeNode: VTreeNode, pgStepTemp: PgStepTemp) {
        if (vTreeNode.isPage()) {
            onPageExposure(vTreeNode, pgStepTemp)
        } else {
            onElementExposure(vTreeNode)
        }
    }

    private fun preDisExposure(vTreeNode: VTreeNode) {
        if (vTreeNode.isPage()) {
            onPageDisExposure(vTreeNode)
        } else {
            onElementDisExposure(vTreeNode)
        }
    }

    private fun onPageExposure(vTreeNode: VTreeNode, pgStepTemp: PgStepTemp) {
        Log.d(TAG, "onPageExposure: $vTreeNode")
        mListenerMgr.startNotify { listener -> listener.onPageView(vTreeNode, pgStepTemp) }
    }

    private fun onPageDisExposure(vTreeNode: VTreeNode) {
        Log.d(TAG, "onPageDisExposure: $vTreeNode")
        mListenerMgr.startNotify { listener -> listener.onPageDisappear(vTreeNode) }
    }

    private fun onElementExposure(vTreeNode: VTreeNode) {
        Log.d(TAG, "onElementExposure: $vTreeNode")
        mListenerMgr.startNotify { listener -> listener.onElementView(vTreeNode) }
    }

    private fun onElementDisExposure(vTreeNode: VTreeNode) {
        Log.d(TAG, "onElementDisExposure: $vTreeNode")
        mListenerMgr.startNotify { listener -> listener.onElementDisappear(vTreeNode) }
    }

    class MarkCallback(
            private val areaMap: HashMap<VTreeNode, Float>,
            private val tempMap: HashMap<VTreeNode, Array<Boolean>>,
            private val index: Int
    ) : ITraverseCallback {

        override fun onEnter(node: VTreeNode, layer: Int): Boolean {
            if (!node.isVisible) {
                return false
            }
            var pairFlag = tempMap[node]
            if (pairFlag == null) {
                pairFlag = arrayOf(false, false)

                tempMap[node] = pairFlag
            }
            pairFlag[index] = true
            areaMap[node] = node.getExposureRate()
            return true
        }

        override fun onLeave(node: VTreeNode, layer: Int) {
        }
    }

    private fun traverseInternal(entry: VTreeNode, order: Boolean, layer: Int, callback: ITraverseCallback) {
        // 回调进入entry的事件
        val node = entry.getNode()
        var flag = true
        if (node != null || entry.isVirtualNode()) {
            flag = callback.onEnter(entry, layer)
        }

        if (flag) {
            val map = mutableMapOf<String, Int?>()
            for (child in entry.childrenList) {
                if (map.containsKey(child.getOid())) {
                    if (map[child.getOid()] == child.getPos()) {
                        ExceptionReporter.reportError(NodeSPMNotUniqueError(child))
                    }
                } else {
                    map[child.getOid()] = child.getPos()
                }
                traverseInternal(child, order, layer + 1, callback)
            }
        }

        if (node != null || entry.isVirtualNode()) {
            callback.onLeave(entry, layer)
        }
    }

    private fun updateChildPage() {
        val tempChildPage = getChildNode(currentExposureVTree)
        val oid = tempChildPage?.getOid() ?: ""
        val spm = tempChildPage?.getSpm() ?: ""
        val oldOid = SPUtils.get(CURRENT_PROCESS_OID, "null")
        val oldSpm = SPUtils.get(CURRENT_PROCESS_SPM, "null")
        if (oid != oldOid || spm != oldSpm) {
            SPUtils.edit().putString(CURRENT_PROCESS_OID, oid).putString(CURRENT_PROCESS_SPM, spm).apply()
            EventDispatch.dispatchChildPageChangeEvent(spm, oid)
        }
    }
}