package com.netease.cloudmusic.datareport.inject.scroll

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import com.netease.cloudmusic.datareport.inject.EventCollector

open class ReportScrollView : ScrollView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        EventCollector.getInstance().onScrollChanged(this)
    }
}