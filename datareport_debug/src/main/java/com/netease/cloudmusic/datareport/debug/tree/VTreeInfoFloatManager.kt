package com.netease.cloudmusic.datareport.debug.tree

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.debug.drawer.NodeRectDrawer
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.traverse.ITraverseCallback
import com.netease.cloudmusic.datareport.vtree.traverse.VTreeTraverser
import java.lang.ref.WeakReference

/**
 * 虚拟树管理类
 * 主要在半透明浮层上面通过不同的框绘制树的每个节点的位置信息
 */
class VTreeInfoFloatManager(target: ViewGroup) {

    private val nodeRectDrawer = NodeRectDrawer(target)

    private var currentClickNode: WeakReference<VTreeNode>? = null

    private var autoView: WeakReference<View>? = null

    private val floatManager = VTreeInfoBoard(target)

    fun clear() {
        currentClickNode = null
        nodeRectDrawer.clear()
        floatManager.clearView()
    }

    fun showContext(){
        floatManager.showContext()
    }

    fun onMotionEventChange(event: MotionEvent?) {
        event?.let {
            VTreeManager.getCurrentVTreeInfo()?.vTreeNode?.let { node ->
                findCurrentClickNode(node, it.rawX.toInt(), it.rawY.toInt())
            }
            autoView = null
            AppEventReporter.getInstance().currentActivity?.window?.decorView?.let { view ->
                val v = findAutoView(view, it.rawX.toInt(), it.rawY.toInt())
                if (v != null) {
                    autoView = WeakReference(v)
                }
            }
            currentClickNode?.get()?.apply {
                nodeRectDrawer.buildNodeLink(this)
            }
            if (autoView != null || currentClickNode != null) {
                floatManager.updateView(currentClickNode?.get(), autoView?.get())
            }
        }
    }

    private fun findCurrentClickNode(node: VTreeNode, x: Int, y: Int) {

        VTreeTraverser.traverse(node, false, nodeTraverseCallback.apply { setClickMotion(x, y) })
    }

    private val nodeTraverseCallback = object : ITraverseCallback {

        private var clickX = 0
        private var clickY = 0
        private var isFind = false

        fun setClickMotion(x: Int, y: Int) {
            clickX = x
            clickY = y
            isFind = false
            currentClickNode = null
        }

        override fun onEnter(node: VTreeNode, layer: Int): Boolean {
            return !isFind && node.isVisible
        }

        override fun onLeave(node: VTreeNode, layer: Int) {
            if (!node.isVisible) {
                return
            }
            if (node.visibleRect.contains(clickX, clickY) && currentClickNode?.get() == null) {
                currentClickNode = WeakReference(node)
                isFind = true
            }
        }
    }

    private fun findAutoView(view: View, x: Int, y: Int): View? {
        if (view is ViewGroup) {
            val count = view.childCount
            for (index in count -1 downTo  0) {
                val childView = findAutoView(view.getChildAt(index), x, y)
                if (childView != null) {
                    return childView
                }
            }
        }
        val off = intArrayOf(0, 0)
        view.getLocationOnScreen(off)
        if (VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(view)?.getSpm() != null && (x > off[0] && y > off[1] && x < off[0] + view.width && y < off[1] + view.height)) {
            return view
        }
        return null
    }

    fun onDraw(canvas: Canvas?) {
        nodeRectDrawer.onDraw(canvas)
    }

}