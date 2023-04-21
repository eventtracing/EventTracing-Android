package com.netease.cloudmusic.datareport.automation

import android.view.View
import com.netease.cloudmusic.datareport.automation.AutomationDataRWProxy.Companion.getDataEntity

object AutomationAttachListener: View.OnAttachStateChangeListener {

    override fun onViewDetachedFromWindow(v: View?) {
        v?.let {
            getDataEntity(it, false)?.apply {
                spm = null
            }
            it.removeOnAttachStateChangeListener(this)
        }
    }

    override fun onViewAttachedToWindow(v: View?) {
    }
}