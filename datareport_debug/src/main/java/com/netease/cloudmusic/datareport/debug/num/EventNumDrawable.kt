package com.netease.cloudmusic.datareport.debug.num

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRectF
import com.netease.cloudmusic.datareport.debug.R

/**
 * 对一个事件的数量进行绘制的drawable
 */
class EventNumDrawable(private val context: Context, var info: String) : Drawable() {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        this.strokeWidth = 3f
        this.textSize = context.resources.getDimension(R.dimen.datareport_icon_text_size)
        this.textAlign = Paint.Align.CENTER;
        this.color = Color.WHITE
    }
    private val iconNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL_AND_STROKE
        this.color = Color.BLACK
        this.alpha = 180
    }
    private val distance: Float
    private val radius: Float
    private val eventNumTextMargin: Float

    init {
        val fontMetrics = textPaint.fontMetrics
        distance =
            (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        radius =
            context.resources.getDimension(R.dimen.datareport_event_text_bg_radius)
        eventNumTextMargin = context.resources.getDimension(R.dimen.datareport_event_margin)
    }

    override fun draw(canvas: Canvas) {
        if (info.isEmpty()) {
            return
        }

        val baseline: Float = bounds.toRectF().centerY() + distance
        canvas.drawRoundRect(bounds.toRectF(), radius, radius, iconNodePaint)
        canvas.drawText(
            info,
            bounds.toRectF().centerX(),
            baseline,
            textPaint
        )
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }
}