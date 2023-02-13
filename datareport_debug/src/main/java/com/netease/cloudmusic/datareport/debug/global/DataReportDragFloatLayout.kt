package com.netease.cloudmusic.datareport.debug.global

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import kotlin.math.sqrt

/**
 * 悬浮按钮
 */
class DataReportDragFloatLayout(context: Context, private val windowManager: WindowManager, private val layoutParams: WindowManager.LayoutParams) : FrameLayout(context) {

    private var lastX: Int = 0
    private var lastY: Int = 0

    private var isDrag: Boolean = false

    private var screenSize = Point()
    init {
        windowManager.defaultDisplay.getSize(screenSize)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val rawX = event.rawX.toInt()
        val rawY = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                isDrag = false
                lastX = rawX
                lastY = rawY
                return super.dispatchTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                isDrag = true
                val dx = rawX - lastX
                val dy = rawY - lastY
                //这里修复一些华为手机无法触发点击事件
                val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
                if (distance == 0) {
                    isDrag = false
                } else {
                    var x = layoutParams.x + dx
                    var y = layoutParams.y + dy
                    //检测是否到达边缘 左上右下
                    x = if (x < 0) 0 else if (x > screenSize.x - width) (screenSize.x - width) else x
                    y = if (getY() < 0) 0 else if (getY() + height > screenSize.y) (screenSize.y - height) else y
                    layoutParams.x = x
                    layoutParams.y = y
                    windowManager.updateViewLayout(this, layoutParams);
                    lastX = rawX
                    lastY = rawY
                }
            }
            MotionEvent.ACTION_UP -> if (!isNotDrag()) {
                //恢复按压效果
                isPressed = false
                if (rawX >= screenSize.x / 2) {
                    //靠右吸附
                    val valueAnimator = ValueAnimator.ofInt(layoutParams.x, screenSize.x - width)
                    valueAnimator.apply {
                        interpolator = DecelerateInterpolator()
                        duration = 500
                        addUpdateListener {
                            layoutParams.x = it.animatedValue as Int
                            windowManager.updateViewLayout(this@DataReportDragFloatLayout, layoutParams)
                        }
                    }.start()
                } else {
                    //靠左吸附
                    val valueAnimator = ValueAnimator.ofInt(layoutParams.x, 0)
                    valueAnimator.apply {
                        interpolator = DecelerateInterpolator()
                        duration = 500
                        addUpdateListener {
                            layoutParams.x = it.animatedValue as Int
                            windowManager.updateViewLayout(this@DataReportDragFloatLayout, layoutParams)
                        }
                    }.start()
                }
            }
        }
        //如果是拖拽则消s耗事件，否则正常传递即可。
        return !isNotDrag() || super.dispatchTouchEvent(event)
    }

    private fun isNotDrag(): Boolean {
        return !isDrag && (x == 0f || x == (screenSize.x - width).toFloat())
    }
}