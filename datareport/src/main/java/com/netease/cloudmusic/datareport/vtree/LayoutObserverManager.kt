package com.netease.cloudmusic.datareport.vtree

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewTreeObserver
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.InnerKey
import java.util.*

object LayoutObserverManager : View.OnAttachStateChangeListener {

    private val viewList = WeakHashMap<View, InternalDrawListener>()

    private val mainHandler = InternalDrawHandler()

    fun openObserver(view: View) {
        val attachedActivity = findAttachedActivity(view)
        if (attachedActivity == null) {
            view.addOnAttachStateChangeListener(this)
        } else {
            attachedActivity.window?.decorView?.let {
                openObserverInternal(it)
            }
        }
    }

    fun closeObserver(view: View) {
        findAttachedActivity(view)?.window?.decorView?.let {
            viewList.remove(it)?.let { listener ->
                it.viewTreeObserver.removeOnDrawListener(listener)
            }
        }
    }

    private fun openObserverInternal(decorView: View) {
        if (DataRWProxy.getInnerParam(decorView, InnerKey.VIEW_ENABLE_LAYOUT_OBSERVER) != true) {
            return
        }
        if (viewList.contains(decorView)) {
            return
        }
        viewList[decorView] = InternalDrawListener(decorView).apply {
            decorView.viewTreeObserver.addOnDrawListener(this)
        }
        decorView.addOnAttachStateChangeListener(object :
                View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                viewList.remove(decorView)?.let {
                    decorView.viewTreeObserver.removeOnDrawListener(it)
                }
            }
        })
    }

    override fun onViewDetachedFromWindow(v: View?) {
    }

    override fun onViewAttachedToWindow(v: View?) {
        findAttachedActivity(v)?.window?.decorView?.let {
            openObserverInternal(it)
        }
    }

    private class InternalDrawHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            VTreeManager.onViewReport(msg.obj as? View)
        }
    }

    private class InternalDrawListener(private val targetView: View) : ViewTreeObserver.OnDrawListener {
        override fun onDraw() {
            if (!mainHandler.hasMessages(targetView.hashCode())) {
                mainHandler.sendMessage(mainHandler.obtainMessage(targetView.hashCode(), targetView))
            }
        }
    }

}