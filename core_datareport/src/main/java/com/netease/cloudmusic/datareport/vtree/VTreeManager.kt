package com.netease.cloudmusic.datareport.vtree

import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.event.IEventType
import com.netease.cloudmusic.datareport.inject.EventCollector
import com.netease.cloudmusic.datareport.inject.fragment.FragmentCompat
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.report.refer.MutableReferStorage
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.scroller.ScrollableViewObserver
import com.netease.cloudmusic.datareport.utils.ListenerMgr
import com.netease.cloudmusic.datareport.utils.Log
import com.netease.cloudmusic.datareport.utils.tracer.SimpleTracer
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.page.DialogListUtil
import com.netease.cloudmusic.datareport.vtree.traverse.ITraverseCallback
import com.netease.cloudmusic.datareport.vtree.traverse.VTreeExposureDetector
import com.netease.cloudmusic.datareport.vtree.traverse.VTreeTraverser
import org.json.JSONArray
import java.util.*

/**
 * 主要监听消息，然后在适当的时机生成 VTree
 */
object VTreeManager: DefaultEventListener(), AppEventReporter.IAppEventListener, View.OnAttachStateChangeListener {

    private const val TAG = "VTreeExposureManager"

    /**
     * 记录在主线程的psRefer和mutableRefer
     * 如果上层需要获取mutableRefer自行使用的话，拿这里的最为合适
     */
    private val psReferMap = WeakHashMap<Activity, MutableMap<Int, String>>()
    private val mutableReferStorage = MutableReferStorage("pre")

    /**
     * 页面切换监听
     */
    interface IVTreeListener {
        fun onVTreeChange(node: VTreeMap?, eventList: List<IEventType>)
    }

    private val mListenerMgr = ListenerMgr<IVTreeListener>()
    private val mDetectionIdleHandler = DetectionIdleHandler()
    private val mDetectionPartialHandler = PartialHandler()
    private var mIsAppForeground = false
    internal val mResumedActivities = Collections.newSetFromMap(WeakHashMap<Activity, Boolean>())

    private var childPage: VTreeNode? = null //缓存最叶子子节点

    private var currentVTreeInfo: VTreeMap? = null
    private var backgroundVTreeInfo : VTreeMap? = null //当app退出到后台，就把退出后台的时候的vtree保存在这里
    fun getCurrentVTreeInfo(): VTreeMap? {
        return if (mIsAppForeground) {
            currentVTreeInfo
        } else {
            backgroundVTreeInfo
        }
    }

    private fun onVTreeChange(node: VTreeMap?, eventList: List<IEventType>){
        childPage = null
        mListenerMgr.startNotify { listener -> listener.onVTreeChange(node, eventList) }
    }

    /**
     * 注册页面切换监听
     */
    fun register(listener: IVTreeListener) {
        mListenerMgr.register(listener)
    }

    /**
     * 反注册页面切换监听
     */
    fun unregister(listener: IVTreeListener) {
        mListenerMgr.unregister(listener)
    }

    override fun onActivityResume(ac: Activity) {
        val activity = changeTransparentActivity(ac) ?: return
        if (!mResumedActivities.contains(activity)) {
            mResumedActivities.add(activity)
        }

        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onActivityResume: activity = $activity")
        }
        val decorView = activity.window?.decorView
        if (decorView == null) {
            if (DataReportInner.getInstance().isDebugMode) {
                Log.d(TAG, "onActivityResume: activity = $activity, null getView()")
            }
            return
        }
        laidOutAppear(activity, decorView)
    }

    override fun onActivityPause(activity: Activity) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onActivityPause: activity = $activity")
        }
        mResumedActivities.remove(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentVTreeInfo?.vTreeNode?.let { updateVTreeClearData(it) }
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onActivityDestroyed: activity = $activity")
        }
        psReferMap.remove(activity)
    }

    /**
     * 当页面被销毁的时候，内部的view什么的都会被销毁，动态的钩子也没有了，要把钩子的内容转成静态的
     */
    private fun updateVTreeClearData(vTreeNode: VTreeNode) {
        VTreeTraverser.traverse(vTreeNode, true, object : ITraverseCallback {
            override fun onEnter(node: VTreeNode, layer: Int): Boolean {
                node.updateDynamicParamsAndDelete()
                return true
            }

            override fun onLeave(node: VTreeNode, layer: Int) {
            }
        })
    }

    override fun onDialogShow(activity: Activity, dialog: Dialog) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onDialogShow: activity = $activity, dialog = $dialog")
        }
        postAppearDetectionTask(activity)
    }

    override fun onDialogHide(activity: Activity, dialog: Dialog) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onDialogHide: activity = " + activity + "dialog =" + dialog)
        }
        postAppearDetectionTask(activity)
    }

    override fun onFragmentPause(fragment: FragmentCompat) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onFragmentPause: fragment=$fragment")
        }
        val activity = fragment.activity
        if (mResumedActivities.contains(activity)) {
            postAppearDetectionTask(activity)
        }
    }

    override fun onFragmentResume(fragment: FragmentCompat) {
        val view = fragment.view
        if (view == null) {
            if (DataReportInner.getInstance().isDebugMode) {
                Log.d(TAG, "onFragmentResume: fragment = $fragment, null getView()")
            }
            return
        }
        laidOutAppear(fragment.activity, view)
    }


    override fun onFragmentDestroyView(fragment: FragmentCompat) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onFragmentDestroyView: fragment = $fragment")
        }
        val view = fragment.view
        if (view == null || view.rootView == null) {
            if (DataReportInner.getInstance().isDebugMode) {
                Log.d(TAG, "onFragmentDestroyView: Fragment = $fragment, null getView()")
            }
            return
        }
        currentVTreeInfo?.treeMap?.get(DataReport.getInstance().getOidParents(view))?.let {
            updateVTreeClearData(it)
        }
        postAppearDetectionTask(findAttachedActivity(view.rootView))
    }

    private fun laidOutAppear(activity: Activity, view: View) {
        val isLaidOut = ViewCompat.isLaidOut(view)
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "laidOutAppear: activity = $activity, isLaidOut = $isLaidOut")
        }
        if (isLaidOut) {
            postAppearDetectionTask(activity)
        } else {
            val onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener =
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        postAppearDetectionTask(activity)
                        view.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                }
            view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
            view.addOnAttachStateChangeListener(object :
                View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {}
                override fun onViewDetachedFromWindow(v: View) {
                    view.viewTreeObserver.removeGlobalOnLayoutListener(onGlobalLayoutListener)
                }
            })
        }
    }

    override fun onViewScroll(scrollView: View) {
        val view = DataReportInner.getInstance().getOidParents(scrollView)
        view?.let { onPartialViewVisible(it, true) }
    }

    override fun onAppOut(isMainThread: Boolean) {
        if (DataReportInner.getInstance().isDebugMode) {
            Log.i(TAG, "onAppOut: ")
        }
        backgroundVTreeInfo = currentVTreeInfo
        currentVTreeInfo = null
        mIsAppForeground = false
        onVTreeChange(null, arrayListOf())
    }

    override fun onAppIn() {
        mIsAppForeground = true
    }

    /**
     * 触发重新构建vtree
     *
     * @param obj 页面对象
     */
    fun onViewReport(obj: Any?) {
        if (obj == null) {
            return
        }
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onPageReport: object=$obj")
        }
        when (obj) {
            is Activity -> {
                postAppearDetectionTask(obj as Activity?)
            }
            is Dialog -> {
                postAppearDetectionTask(DialogListUtil.getDialogActivity(obj as Dialog?))
            }
            is View -> {
                onPageViewVisible(obj as View?)
            }
        }
    }

    /**
     * 局部生成树
     */
    fun onPartialViewVisible(view: View?, forceUpdate: Boolean = false) {
        if (view != null && !mDetectionPartialHandler.hasMessages(view.hashCode())) {
            mDetectionPartialHandler.sendMessage(Message.obtain(mDetectionPartialHandler, view.hashCode(), view).apply {
                arg1 = if(forceUpdate) 1 else 0
            })
        }
    }

    /**
     * 当根布局发生了滚动事件时的处理，需要生成整棵树
     */
    fun onRootScroll(view: View?) {
        if (view == null) {
            return
        }
        val attachedActivity = findAttachedActivity(view)
        if (attachedActivity != null) {
            postAppearDetectionTask(attachedActivity)
        }
    }

    fun onPageViewVisible(view: View?) {
        if (view == null || !ScrollableViewObserver.getInstance().isIdle) {
            return
        }
        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "onPageViewVisible: view = $view")
        }

        val attachedActivity = findAttachedActivity(view)
        if (attachedActivity == null) {
            view.addOnAttachStateChangeListener(this)
        } else {
            postAppearDetectionTask(attachedActivity)
        }
    }

    /**
     * 当发生了自定义事件或者其他什么事件时，对应的View没有生成。就需要先生成树再处理
     */
    fun updateVTreeForEvent(activity: Activity?, eventType: IEventType) {
        postAppearDetectionTask(activity, eventType)
    }

    private fun postAppearDetectionTask(ac: Activity?, eventType: IEventType? = null) {
        val activity = changeTransparentActivity(ac)

        if (DataReportInner.getInstance().isDebugMode) {
            Log.d(TAG, "postAppearDetectionTask: activity = $activity")
        }
        if (activity == null || !mResumedActivities.contains(activity)) {
            if (DataReportInner.getInstance().isDebugMode) {
                Log.d(TAG, "postAppearDetectionTask: unable to detect activity")
            }
            return
        }

        eventType?.let { mDetectionIdleHandler.reportList.add(it) }
        if (!mDetectionIdleHandler.hasMessages(activity.hashCode())) {
            mDetectionIdleHandler.sendMessage(mDetectionIdleHandler.obtainMessage(activity.hashCode(), activity))
        }
    }

    private fun detectActivePage(activity: Activity) {
        val tag = "PageSwitchObserver.detectActivity($activity)"
        SimpleTracer.begin(tag)
        activity.window?.decorView?.let {
            val dialogList = (DialogListUtil.getDialogList(activity)?: listOf()).mapNotNull { item-> item.get()?.window?.decorView }
            val pageInfo = VTreeExposureDetector.detectAndBuild(it, dialogList, false)

            //生成了一棵新的虚拟树，这里需要和老的虚拟树比较一下
            val pageInfoHashCode = pageInfo?.rootPage?.hashCode()
            if (pageInfoHashCode != null && pageInfoHashCode != currentVTreeInfo?.rootPage?.hashCode()) {
                var map = psReferMap[activity]
                if (map == null) {
                    map = mutableMapOf()
                    psReferMap[activity] = map
                }

                val psRefer = if (!map.containsKey(pageInfoHashCode)) {
                    ReferManager.getMainThreadPrePsRefer()?.apply {
                        map[pageInfoHashCode] = this
                    }
                }else{
                    map[pageInfoHashCode]
                }

                mutableReferStorage.onRootExposure(psRefer)
                ReferManager.onMainThreadRootView(pageInfo.rootPage.getNode())
            }

            currentVTreeInfo = pageInfo
            val tempEventList: MutableList<IEventType> = mutableListOf()
            tempEventList.addAll(mDetectionIdleHandler.reportList)
            mDetectionIdleHandler.reportList.clear()
            onVTreeChange(pageInfo, tempEventList)
        }
        SimpleTracer.end(tag)
    }

    private fun detectPartialPage(view: View) {

        val partialNode = getCurrentVTreeInfo()?.treeMap?.get(view)
        if (partialNode == null || getRootPageOrRootElement(partialNode) == partialNode) {
            onRootScroll(view)
            return
        }

        val pageInfo = VTreeExposureDetector.detectAndBuild(view, null, true)

        pageInfo?.let {
            val partialPage = copyVTreeFix(currentVTreeInfo, it)
            if (partialPage != null) {
                currentVTreeInfo = partialPage
                onVTreeChange(partialPage, listOf())
            }
        }
    }

    private class DetectionIdleHandler : Handler(Looper.getMainLooper()) {

        val reportList = mutableListOf<IEventType>()

        override fun handleMessage(msg: Message) {
            var activity = msg.obj as? Activity?
            activity = changeTransparentActivity(activity)
            if (!mIsAppForeground || activity == null || activity.isFinishing) {
                return
            }
            detectActivePage(activity)
        }
    }

    private class PartialHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            (msg.obj as? View?)?.let {
                val ac = findAttachedActivity(it.rootView)
                if (ac == null || ac.isFinishing) {
                    return
                }
                detectPartialPage(it)
            }
        }
    }

    init {
        EventCollector.getInstance().registerEventListener(this)
        AppEventReporter.getInstance().register(this)
    }

    override fun onViewDetachedFromWindow(v: View?) {
        v?.removeOnAttachStateChangeListener(this)
    }

    override fun onViewAttachedToWindow(v: View?) {
        v?.removeOnAttachStateChangeListener(this)
        onPageViewVisible(v)
    }

    /**
     * 获取rootpage 的spm
     */
    fun getRootPageSpm(): String? {
        return getCurrentVTreeInfo()?.rootPage?.getSpm()
    }

    /**
     * 获取rootpage 的oid
     */
    fun getRootPageOid(): String? {
        return getCurrentVTreeInfo()?.rootPage?.getOid()
    }

    /**
     * 获取最叶子子节点的 spm
     */
    fun getChildPageSpm(): String? {
        if (childPage == null) {
            childPage = getChildNode(getCurrentVTreeInfo()?.vTreeNode)
        }
        return childPage?.getSpm()
    }

    /**
     * 获取最叶子子节点的 oid
     */
    fun getChildPageOid(): String? {
        if (childPage == null) {
            childPage = getChildNode(getCurrentVTreeInfo()?.vTreeNode)
        }
        return childPage?.getOid()
    }

    fun getMutableRefer(): String {
        return mutableReferStorage.getMutableRefer()
    }

    fun getMutableReferArray(): JSONArray {
        return mutableReferStorage.getMutableReferForArray()
    }

    fun clear() {
        mutableReferStorage.clear()
    }
}