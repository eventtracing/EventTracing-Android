package com.netease.cloudmusic.datareport.event

import android.view.View
import com.netease.cloudmusic.datareport.scroller.ScrollInfo
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * 滑动事件类型
 */
class ScrollEventType(targetView: View?, private val scrollInfo: ScrollInfo) : IEventType {

    companion object {
        private const val SCROLL_PARAMS = "es_params"
        private const val OFFSET_PARAMS = "offset"
        private const val DESTINATION = "destination"
        private const val KEY_X = "x"
        private const val KEY_Y = "y"
        private const val MODE = "mode"
    }

    private val viewReference = WeakReference<View>(targetView)

    override fun getEventType(): String {
        return EventKey.VIEW_SCROLL
    }

    override fun getTarget(): Any? {
        return viewReference.get()
    }

    override fun getParams(): Map<String, Any> {
        val offset = mutableMapOf<String, Any>(
            Pair(KEY_X, scrollInfo.offsetX),
            Pair(KEY_Y, scrollInfo.offsetY)
        )
        val destination = mutableMapOf<String, Any>(
            Pair(KEY_X, scrollInfo.destinationX),
            Pair(KEY_Y, scrollInfo.destinationY)
        )
        val params = mutableMapOf<String, Any>(Pair(OFFSET_PARAMS, offset), Pair(DESTINATION, destination), Pair(MODE, scrollInfo.mode))

        return mutableMapOf(Pair(SCROLL_PARAMS, JSONObject(params.toMap()).toString()))
    }

    override fun isContainsRefer(): Boolean {
        return false
    }

    override fun isActSeqIncrease(): Boolean {
        return false
    }


}