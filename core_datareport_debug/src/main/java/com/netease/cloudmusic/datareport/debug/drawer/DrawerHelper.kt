package com.netease.cloudmusic.datareport.debug.drawer

import android.graphics.Rect
import android.graphics.RectF
import com.netease.cloudmusic.datareport.utils.SafeList

/**
 * 调整数字重叠的情况，尽量把他们都错开一个身位
 */
fun fixEventNumRect(list: SafeList<RectF>, layer: Int, fixSize: Float) {
    if (layer < 2) {
        return
    }
    val targetRect = list[layer - 1]

    for (index in 0 until layer - 1) {
        val tempRect = list[index]
        if (RectF.intersects(tempRect, targetRect)) {
            targetRect.set(tempRect.left - fixSize - targetRect.width(), targetRect.top, tempRect.left - fixSize, targetRect.bottom)
        }
    }
}

/**
 * 调整节点的框重叠的情况，尽量把他们都错开一个像素
 */
fun fixPageRect(list: List<PageRectInfo>, fixSize: Int) {

    if (list.size > 1) {
        for (index in 1..list.size - 1) {
            val current = list[index]
            for (pIndex in 0..index - 1) {
                val parentRectInfo = list[pIndex]
                checkoutLineCoincide(parentRectInfo, current, fixSize)
                checkoutIconCoincide(parentRectInfo.iconRect, current.iconRect)
            }
        }
    }
}

private fun checkoutLineCoincide(parentInfo: PageRectInfo, changeInfo: PageRectInfo, fixSize: Int) {
    if (parentInfo.rectInfo.left == changeInfo.rectInfo.left) {
        if (changeInfo.iconRect.left == changeInfo.rectInfo.left) {
            changeInfo.rectInfo.left += fixSize
            changeInfo.iconRect.offsetTo(changeInfo.rectInfo.left, changeInfo.iconRect.top)
        } else {
            changeInfo.rectInfo.left += fixSize
        }
    }
    if (parentInfo.rectInfo.right == changeInfo.rectInfo.right) {
        changeInfo.rectInfo.right -= fixSize
    }
    if (parentInfo.rectInfo.top == changeInfo.rectInfo.top) {
        changeInfo.rectInfo.top += fixSize
        changeInfo.iconRect.offsetTo(changeInfo.iconRect.left, changeInfo.rectInfo.top)
    }
    if (parentInfo.rectInfo.bottom == changeInfo.rectInfo.bottom) {
        changeInfo.rectInfo.bottom -= fixSize
    }
}
private fun checkoutIconCoincide(parentRect: Rect, changeRect: Rect) {
    if (Rect.intersects(parentRect, changeRect)) {
        val width = changeRect.width()
        changeRect.left = parentRect.right
        changeRect.right = changeRect.left + width
    }
}