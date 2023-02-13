package com.netease.cloudmusic.datareport.inject.scroll

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import com.netease.cloudmusic.datareport.inject.EventCollector

open class ReportNestedScrollView : NestedScrollView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        EventCollector.getInstance().onScrollChanged(this)
    }
}