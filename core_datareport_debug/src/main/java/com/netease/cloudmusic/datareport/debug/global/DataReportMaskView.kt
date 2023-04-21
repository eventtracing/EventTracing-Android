package com.netease.cloudmusic.datareport.debug.global

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.widget.FrameLayout
import com.netease.cloudmusic.datareport.debug.tree.VTreeInfoFloatManager

/**
 * 信息浮层 在这个浮层上面展示所有的信息
 */
class DataReportMaskView(context: Context) : FrameLayout(context) {
    private val manager: VTreeInfoFloatManager = VTreeInfoFloatManager(this)


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        manager.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        DataReportDragManager.closeDashboard()
        DataReportDragManager.closeErrorLayout()

        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                manager.onMotionEventChange(event)
                postInvalidate()
            }
        }
        return true
    }

    fun clear(){
        manager.clear()
        postInvalidate()
    }
    fun showContext() {
        manager.showContext()
    }

}