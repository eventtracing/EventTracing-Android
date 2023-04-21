package com.netease.cloudmusic.datareport.inject.activity

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.LayoutInflaterFactory
import com.netease.cloudmusic.datareport.inject.scroll.ReportHorizontalScrollView
import com.netease.cloudmusic.datareport.inject.scroll.ReportNestedScrollView
import com.netease.cloudmusic.datareport.inject.scroll.ReportScrollView

/**
 * 对scrollview进行替换
 */
class ScrollFactory : LayoutInflaterFactory {

    companion object {
        fun getScrollView(preResult: View?, parent: View?, name: String?, context: Context, attrs: AttributeSet): View? {
            if (preResult != null) {
                return preResult
            }
            return when (name) {
                "ScrollView" -> {
                    ReportScrollView(context, attrs)
                }
                "NestedScrollView" -> {
                    ReportNestedScrollView(context, attrs)
                }
                "HorizontalScrollView" -> {
                    ReportHorizontalScrollView(context, attrs)
                }
                else -> {
                    null
                }
            }
        }
    }

    override fun onCreateView(
        parent: View?,
        name: String?,
        context: Context,
        attrs: AttributeSet
    ): View? {
        return getScrollView(null, parent, name, context, attrs)
    }
}