package com.netease.cloudmusic.datareport.event

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.provider.IChildPageChangeCallback
import com.netease.cloudmusic.datareport.provider.IEventCallback
import com.netease.cloudmusic.datareport.provider.INodeEventCallback
import com.netease.cloudmusic.datareport.provider.IViewEventCallback
import com.netease.cloudmusic.datareport.vtree.*
import com.netease.cloudmusic.datareport.report.CustomReport
import com.netease.cloudmusic.datareport.report.ViewClickReport
import com.netease.cloudmusic.datareport.report.WebReport
import com.netease.cloudmusic.datareport.utils.Log
import com.netease.cloudmusic.datareport.utils.UIUtils
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.exposure.VTreeExposureManager
import java.lang.ref.WeakReference

/**
 * 所有的事件都汇总到这个类来分发，主要是为了让所有事件都在一个线程按顺序执行
 * 目前主要有 vtree变更，点击，自定义 事件
 */
object EventDispatch : VTreeManager.IVTreeListener {

    private const val TAG = "EventDispatch"

    private const val CODE_TREE_CHANGE = 1

    /**
     * 初始化一个埋点的looper线程
     */
    private val exposureThread = HandlerThread("exposure_thread")
    private val handler: ExposureHandler

    /**
     * 主要用于回调
     */
    private val mUpdateHandler = Handler(Looper.getMainLooper())

    private val eventCallbackList = ArrayList<WeakReference<IEventCallback>>()
    private val viewEventCallbackList = ArrayList<WeakReference<IViewEventCallback>>()
    private val nodeEventCallbackList = ArrayList<WeakReference<INodeEventCallback>>()
    private val childPageChangeCallbackList = ArrayList<WeakReference<IChildPageChangeCallback>>()

    class ExposureHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                CODE_TREE_CHANGE -> {
                    val tempVTreeNode = msg.obj as? VTreeMap?
                    VTreeExposureManager.onVTreeChange(tempVTreeNode)
                }
                else -> {
                }
            }
        }
    }

    init {
        exposureThread.start()
        handler =
            ExposureHandler(
                exposureThread.looper
            )
        VTreeManager.register(this)
    }

    override fun onVTreeChange(node: VTreeMap?, eventList: List<IEventType>) {
        handler.sendMessage(Message.obtain(
            handler,
            CODE_TREE_CHANGE, node))
        for (item in eventList) {
            if (item.getEventType() == EventKey.VIEW_CLICK) {
                viewClickEvent(item, null)
            } else {
                customReportEvent(item, null)
            }
        }
    }

    private fun customReportEvent(customEvent: IEventType, node: VTreeNode?) {
        handler.post {
            if (customEvent is WebEventType) {
                WebReport.webReportEvent(customEvent, node)
            } else {
                CustomReport.customReportEvent(customEvent, node)
            }
        }
    }

    private fun viewClickEvent(clickEvent: IEventType, node: VTreeNode?) {
        handler.post {
            if (node != null) {
                ViewClickReport.viewClickEventWithNode(clickEvent, node)
            } else {
                ViewClickReport.viewClickEvent(clickEvent)
            }
        }
    }

    fun onEventNotifier(eventType: IEventType, node: VTreeNode? = null) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.debug(TAG, "onEventNotifier: eventType : ${eventType.getEventType()}")
        }
        callbackEvent(eventType.getEventType(), node)

        postUpdateVTree(eventType, node)
    }

    private fun postUpdateVTree(eventType: IEventType, node: VTreeNode?) {
        val target = eventType.getTarget()
        if (target == null) {
            customReportEvent(eventType, null)
        } else {
            val run = Runnable {
                var innerNode: VTreeNode? = node
                if (innerNode == null) {
                    val view = getView(target)
                    if (view != null) {
                        innerNode = VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(view)
                    }
                }

                if (innerNode != null) {
                    when {
                        eventType is WebEventType -> {
                            customReportEvent(eventType, innerNode)
                        }
                        eventType.getEventType() == EventKey.VIEW_CLICK -> {
                            viewClickEvent(eventType, innerNode)
                        }
                        else -> {
                            customReportEvent(eventType, innerNode)
                        }
                    }
                } else {
                    VTreeManager.updateVTreeForEvent(findAttachedActivity(getView(eventType.getTarget())), eventType)
                }
            }
            if (UIUtils.isMainThread()) {
                run.run()
            } else {
                mUpdateHandler.post(run)
            }
        }
    }

    fun postRunnable(runnable: Runnable) {
        handler.post(runnable)
    }

    fun addEventCallback(callback: IEventCallback) {
        eventCallbackList.add(WeakReference(callback))
    }

    fun addViewEventCallback(callback: IViewEventCallback) {
        viewEventCallbackList.add(WeakReference(callback))
    }

    fun addNodeEventCallback(callback: INodeEventCallback) {
        nodeEventCallbackList.add(WeakReference(callback))
    }

    fun addChildPageChangeCallback(callback: IChildPageChangeCallback) {
        childPageChangeCallbackList.add(WeakReference(callback))
    }

    fun dispatchChildPageChangeEvent(childPageSpm: String?, childPageOid: String?) {
        postMainRunnable(object : Runnable {
            override fun run() {
                childPageChangeCallbackList.forEach {
                    it.get()?.onChildPageOidChange(childPageSpm, childPageOid, AppEventReporter.getInstance().isCurrentProcessAppForeground)
                }
            }
        })
    }

    fun removeViewEventCallback(callback: IViewEventCallback) {
        val viewIterator = viewEventCallbackList.iterator()
        while (viewIterator.hasNext()) {
            val eventCallback = viewIterator.next().get()
            if (eventCallback == callback) {
                viewIterator.remove()
                break
            }
        }
    }

    fun removeNodeEventCallback(callback: INodeEventCallback) {
        val nodeIterator = nodeEventCallbackList.iterator()
        while (nodeIterator.hasNext()) {
            val eventCallback = nodeIterator.next().get()
            if (eventCallback == callback) {
                nodeIterator.remove()
                break
            }
        }
    }

    fun postMainRunnable(runnable: Runnable) {
        mUpdateHandler.post(runnable)
    }

    fun callbackEvent(event: String, node: VTreeNode?) {

        if (node == null) {
            return
        }
        val iterator = eventCallbackList.iterator()
        while (iterator.hasNext()) {
            val eventCallback = iterator.next().get()
            if (eventCallback == null) {
                iterator.remove()
            } else {
                mUpdateHandler.post {
                    eventCallback.onEvent(event, node.hashCode())
                }
            }
        }

        val viewIterator = viewEventCallbackList.iterator()
        while (viewIterator.hasNext()) {
            val eventCallback = viewIterator.next().get()
            if (eventCallback == null) {
                viewIterator.remove()
            } else {
                mUpdateHandler.post {
                    eventCallback.onEvent(event, node.getNode())
                }
            }
        }

        val nodeIterator = nodeEventCallbackList.iterator()
        while (nodeIterator.hasNext()) {
            val eventCallback = nodeIterator.next().get()
            if (eventCallback == null) {
                nodeIterator.remove()
            } else {
                mUpdateHandler.post {
                    eventCallback.onEvent(event, node)
                }
            }
        }
    }
}