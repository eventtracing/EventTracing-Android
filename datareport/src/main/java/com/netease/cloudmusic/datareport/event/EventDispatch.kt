package com.netease.cloudmusic.datareport.event

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.provider.IViewEventCallback
import com.netease.cloudmusic.datareport.vtree.*
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

    private val viewEventCallbackList = ArrayList<WeakReference<IViewEventCallback>>()

    class ExposureHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                CODE_TREE_CHANGE -> {
                    val tempVTreeNode = msg.obj as? VTreeMap?
                    VTreeExposureManager.onVTreeChange(tempVTreeNode)
                }
                else -> {}
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
        handler.sendMessage(Message.obtain(handler, CODE_TREE_CHANGE, node))
        for (item in eventList) {
            item.reportEvent(null)
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
            eventType.reportEvent(null)
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
                    eventType.reportEvent(innerNode)
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

    fun addViewEventCallback(callback: IViewEventCallback) {
        viewEventCallbackList.add(WeakReference(callback))
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

    fun postMainRunnable(runnable: Runnable) {
        mUpdateHandler.post(runnable)
    }

    fun callbackEvent(event: String, node: VTreeNode?) {
        if (node == null) {
            return
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
    }
}