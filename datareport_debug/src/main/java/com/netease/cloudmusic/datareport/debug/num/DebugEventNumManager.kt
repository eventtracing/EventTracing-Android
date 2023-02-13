package com.netease.cloudmusic.datareport.debug.num

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.View
import com.netease.cloudmusic.datareport.debug.R
import com.netease.cloudmusic.datareport.debug.drawer.fixEventNumRect
import com.netease.cloudmusic.datareport.debug.global.DataReportDragManager
import com.netease.cloudmusic.datareport.debug.global.DragEventListener
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.event.IEventType
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.provider.IViewEventCallback
import com.netease.cloudmusic.datareport.utils.SafeList
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.traverse.ITraverseCallback
import com.netease.cloudmusic.datareport.vtree.traverse.VTreeTraverser
import java.util.*

class DebugEventNumManager(private val context: Context) : IViewEventCallback, VTreeManager.IVTreeListener, DragEventListener {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        this.strokeWidth = 3f
        this.textSize = context.resources.getDimension(R.dimen.datareport_icon_text_size)
        this.textAlign = Paint.Align.CENTER;
        this.color = Color.WHITE
    }

    private val eventNumTextMargin: Float = context.resources.getDimension(R.dimen.datareport_event_margin)
    private val numTextMap = WeakHashMap<View, EventNumDrawable>()

    private val eventExposureInfo = mutableMapOf<Int, Int>()
    private val eventClickInfo = mutableMapOf<Int, Int>()
    private val eventSlideInfo = mutableMapOf<Int, Int>()

    private var dashboardCheckedIndex: Int = 0

    fun setType(type: Int) {
        if (this.dashboardCheckedIndex == type) {
            return
        }
        if(this.dashboardCheckedIndex != 0){
            clearViewsNumText()
        }
        this.dashboardCheckedIndex = type
        onVTreeChange(VTreeManager.getCurrentVTreeInfo(), listOf())
    }

    private fun clearViewsNumText() {
        numTextMap.forEach {
            it.value.info = ""
            it.key.invalidate()
        }
    }

    init {
        DataReportDragManager.registerEventCallback(this)
        DataReport.getInstance().addViewEventCallback(this)
        VTreeManager.register(this)
    }

    fun onDetachedFromWindow() {
        DataReportDragManager.unRegisterEventCallback(this)
        VTreeManager.unregister(this)
    }

    override fun onEvent(event: String, view: View?) {
        when (event) {
            EventKey.PAGE_VIEW, EventKey.ELEMENT_VIEW -> {
                onInnerEvent(eventExposureInfo, VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(view)?.hashCode()?:return)
            }
            EventKey.VIEW_CLICK -> {
                onInnerEvent(eventClickInfo, VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(view)?.hashCode()?:return)
            }
            EventKey.VIEW_SCROLL -> {
                onInnerEvent(eventSlideInfo, VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(view)?.hashCode()?:return)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private fun onInnerEvent(eventInfo: MutableMap<Int, Int>, hashCode: Int) {
        val num = eventInfo[hashCode] ?: 0
        eventInfo[hashCode] = num + 1

        handler.removeCallbacks(vTreeChangeTask)
        handler.post(vTreeChangeTask)
    }

    private val vTreeChangeTask = Runnable { onVTreeChange(VTreeManager.getCurrentVTreeInfo(), listOf()) }

    private fun getExposureEventNumInfo(): MutableMap<Int, Int> {
        return eventExposureInfo
    }

    private fun getClickEventNumInfo(): MutableMap<Int, Int> {
        return eventClickInfo
    }

    private fun getSlideEventNumInfo(): MutableMap<Int, Int> {
        return eventSlideInfo
    }

    private val tempList = object : SafeList<RectF>(10) {
        override fun initValue(): RectF {
            return RectF()
        }
    }

    override fun onVTreeChange(vTreeMap: VTreeMap?, eventList: List<IEventType>) {
        vTreeMap ?: return
        when (dashboardCheckedIndex) {
            DataReportDragManager.SHOW_TYPE_EXPOSURE -> getExposureEventNumInfo()
            DataReportDragManager.SHOW_TYPE_CLICK -> getClickEventNumInfo()
            DataReportDragManager.SHOW_TYPE_SLIDE -> getSlideEventNumInfo()
            else -> null
        }?.let { map ->
            tempList.clear()
            vTreeMap.vTreeNode?.let { node ->
                VTreeTraverser.traverse(node, true, object : ITraverseCallback {
                    override fun onEnter(node: VTreeNode, layer: Int): Boolean {
                        map[node.hashCode()]?.let { num ->
                            val text = num.toString()
                            val length = textPaint.measureText(text)
                            val visibleRect = node.visibleRect
                            val rect = RectF(
                                visibleRect.right - length - 2 * context.resources.getDimension(
                                    R.dimen.datareport_event_text_padding_vertical
                                ),
                                visibleRect.top.toFloat(),
                                visibleRect.right.toFloat(),
                                visibleRect.top + textPaint.textSize + 2 * context.resources.getDimension(
                                    R.dimen.datareport_event_text_padding_horizontal
                                )
                            )
                            tempList[layer - 1] = rect
                            fixEventNumRect(tempList, layer, eventNumTextMargin)
                            node.getNode()?.let {
                                var drawable = numTextMap[it]
                                if (drawable == null) {
                                    drawable = EventNumDrawable(it.context, text)
                                    numTextMap[it] = drawable
                                    val overlay = it.overlay
                                    overlay.add(drawable)
                                }
                                it.getGlobalVisibleRect(tempRect)
                                drawable.setBounds((rect.left - tempRect.left).toInt(), 0, (rect.right - tempRect.left).toInt(), rect.height().toInt())
                                drawable.info = text
                                it.invalidate()
                            }
                        }
                        return true
                    }
                    override fun onLeave(node: VTreeNode, layer: Int) {
                    }
                })
            }
        }
    }

    private val tempRect: Rect = Rect()
    override fun setEventNumType(type: Int) {
        setType(type)
    }
}