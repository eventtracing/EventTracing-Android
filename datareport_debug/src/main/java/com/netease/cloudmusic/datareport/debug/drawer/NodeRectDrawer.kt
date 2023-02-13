package com.netease.cloudmusic.datareport.debug.drawer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.ViewGroup
import com.netease.cloudmusic.datareport.debug.R
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

/**
 * 绘制每个节点的框框
 */
class NodeRectDrawer(private val target: ViewGroup) {

    companion object {
        private val colorList = arrayOf(0xFFFF5B5B.toInt(), 0xFFFF9E38.toInt(), 0xFF00B894.toInt(), 0xFF5D7CFF.toInt(), 0xFFA382FF.toInt(), 0xFFEC78FF.toInt(), 0xFFC55994.toInt())
    }

    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.strokeWidth = target.context.resources.getDimension(R.dimen.datareport_line_width)
        this.style = Paint.Style.STROKE
    }

    private val iconNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL_AND_STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        this.strokeWidth = 3f
        this.textSize = target.context.resources.getDimension(R.dimen.datareport_icon_text_size)
        this.textAlign = Paint.Align.CENTER;
        this.color = Color.WHITE
    }

    private val currentNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = 0x1A0091FF.toInt()
        this.style = Paint.Style.FILL_AND_STROKE
    }

    private val pageRectList = mutableListOf<PageRectInfo>()
    private val elementList = mutableListOf<Rect>()

    fun buildNodeLink(vTreeNode: VTreeNode) {
        pageRectList.clear()
        elementList.clear()
        getNodeLink(pageRectList, elementList, vTreeNode)
        fixPageRect(pageRectList, target.context.resources.getDimension(R.dimen.datareport_line_width).toInt())
    }

    private fun getNodeLink(
        pageList: MutableList<PageRectInfo>,
        elementList: MutableList<Rect>,
        node: VTreeNode) {

        val off = intArrayOf(0, 0)
        target.getLocationOnScreen(off)

        var parentNode: VTreeNode? = node
        val fixSize = target.context.resources.getDimension(R.dimen.datareport_line_width).toInt()
        while (parentNode?.parentNode != null) {
            if (parentNode.isPage()) {
                val rect = parentNode.visibleRect
                val width = target.context.resources.getDimension(R.dimen.datareport_icon_size).toInt()
                pageList.add(0,
                    PageRectInfo(
                        Rect(rect.left + fixSize, rect.top + fixSize, rect.right - fixSize, rect.bottom - fixSize).apply { fixRect(this, off, fixSize) },
                        Rect(rect.left + fixSize, rect.top + fixSize, rect.left + width, rect.top + width).apply { fixRect(this, off, fixSize) }
                    )
                )
            } else {
                val rect = parentNode.visibleRect
                val width = target.context.resources.getDimension(R.dimen.datareport_icon_size).toInt()
                pageList.add(0,
                    PageRectInfo(
                        Rect(rect.left + fixSize, rect.top + fixSize, rect.right - fixSize, rect.bottom - fixSize).apply { fixRect(this, off, fixSize) },
                        Rect(rect.left + fixSize, rect.top + fixSize, rect.left + width, rect.top + width).apply { fixRect(this, off, fixSize) }
                    )
                )
                elementList.add(0, Rect(parentNode.visibleRect).apply { fixRect(this, off, fixSize) })
            }
            parentNode = parentNode.parentNode
        }
    }

    private fun fixRect(rect: Rect, array: IntArray, fixSize: Int) {
        rect.offset(-array[0], -array[1])
        if (rect.left < fixSize) {
            rect.offsetTo(fixSize, rect.top)
        }
        if (rect.top < fixSize) {
            rect.offsetTo(rect.left, fixSize)
        }
    }

    fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawPageRect(canvas, pageRectList)
            drawElementRect(canvas, elementList)
        }
    }

    private fun drawElementRect(canvas: Canvas, list: List<Rect>) {
        currentNodePaint.color = 0x1A0091FF.toInt()
        for (index in list.indices) {
            if (index == list.size - 1) {
                currentNodePaint.color = 0x4D0091FF.toInt()
            }
            canvas.drawRect(list[index], currentNodePaint)
        }
    }

    private fun drawPageRect(canvas: Canvas, list: List<PageRectInfo>) {
        list.forEachIndexed { index, info ->
            val colorIndex = if (index >= colorList.size) {
                colorList.size - 1
            } else {
                index
            }
            nodePaint.color = colorList[colorIndex]
            canvas.drawRect(info.rectInfo, nodePaint)

            iconNodePaint.color = colorList[colorIndex]
            canvas.drawRect(info.iconRect, iconNodePaint)

            val text = (index + 1).toString()
            //计算baseline
            val fontMetrics = textPaint.fontMetrics
            val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
            val baseline: Float = info.iconRect.centerY() + distance
            canvas.drawText(text, info.iconRect.centerX().toFloat(), baseline, textPaint)
        }
    }

    fun clear() {
        pageRectList.clear()
        elementList.clear()
    }
}