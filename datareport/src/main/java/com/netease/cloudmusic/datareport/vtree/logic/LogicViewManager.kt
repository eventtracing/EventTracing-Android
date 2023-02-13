package com.netease.cloudmusic.datareport.vtree.logic

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.utils.ViewCompatUtils
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.findAttachedActivity
import java.lang.ref.WeakReference

/**
 * 逻辑挂靠管理类，记录所有定义了逻辑挂靠的view，根据Activity为一个纬度
 * 当activity destroy的时候会销毁
 */
object LogicViewManager {

    private val logicViewStorage = LogicViewStorage()

    fun getAlertViewList(activity: Activity?): List<WeakReference<View?>> {
        return logicViewStorage.getAlertViewList(activity)
    }

    /**
     * 获取root节点抢占的view
     */
    fun getRootPage(activity: Activity?): View? {
        return logicViewStorage.getRootView(activity)
    }

    private fun setAlertViewInner(view: View, isAlertView: Boolean) {
        findAttachedActivity(view)?.let {
            if (isAlertView) {
                logicViewStorage.addAlertView(it, view)
            } else {
                logicViewStorage.deleteAlertView(it, view)
            }
        }
    }

    private fun setRootViewInner(view: View) {
        findAttachedActivity(view)?.let {
            logicViewStorage.setRootView(it, view)
        }
    }

    fun setRootView(view: View) {
        setViewInner(view) {
            setRootViewInner(view)
        }
    }

    fun setAlertView(view: View, isAlertView: Boolean) {
        setViewInner(view) {
            setAlertViewInner(view, isAlertView)
        }
    }

    private fun setViewInner(view: View, block:() -> Unit){
        val isAttachedToWindow = ViewCompatUtils.isAttachedToWindow(view)

        if (isAttachedToWindow) {
            block.invoke()
        } else {
            val onPreDrawListener: ViewTreeObserver.OnPreDrawListener = object : ViewTreeObserver.OnPreDrawListener{
                override fun onPreDraw(): Boolean {
                    block.invoke()
                    view.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            }
            view.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
            view.addOnAttachStateChangeListener(object :
                    View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {}
                override fun onViewDetachedFromWindow(v: View) {
                    view.viewTreeObserver.removeOnPreDrawListener(onPreDrawListener)
                }
            })
        }
    }

    fun onActivityDestroy(activity: Activity?) {
        logicViewStorage.onActivityDestroy(activity)
        if (DataRWProxy.isTransparentActivity(activity)) {
            val preActivity = AppEventReporter.getInstance().getPreActivity(activity)
            val iterator = logicViewStorage.getAlertViewList(preActivity).iterator()
            while (iterator.hasNext()) {
                if (iterator.next().get()?.context == activity) {
                    iterator.remove()
                }
            }
            VTreeManager.onViewReport(preActivity)
        }
    }
}