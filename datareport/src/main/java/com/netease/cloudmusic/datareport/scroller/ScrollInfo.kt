package com.netease.cloudmusic.datareport.scroller

import androidx.recyclerview.widget.RecyclerView

data class ScrollInfo(
    var scrollEventEnable: Boolean = false,
    var scrollState: Int = RecyclerView.SCROLL_STATE_IDLE,
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    var destinationX: Int = 0,
    var destinationY: Int = 0,
    var mode: String = DISTANCE){
    companion object{
        const val DISTANCE = "distance"
        const val CELL = "cell"
    }
}