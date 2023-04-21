package com.netease.cloudmusic.datareport.vtree.traverse

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.netease.cloudmusic.datareport.data.DataEntity
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.utils.ViewCompatUtils
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap

class VTreeExposureDetector {

    companion object {
        /**
         * 主要是做可见性检查
         * @param entryView   入口视图
         */
        fun detectAndBuild(entryView: View?, listDialogView: List<View>?, isPartial: Boolean): VTreeMap? {
            if (entryView == null) {
                return null
            }
            val detectionData = DetectionData()
            val ancestor = detectionData.ancestorsInfo[0]
            if (entryView.parent == null || entryView.parent !is ViewGroup) {
                if (entryView.visibility != View.VISIBLE) {
                    return null
                }
                val location = IntArray(2)
                entryView.getLocationOnScreen(location)
                ancestor.visibleRect.set(location[0], location[1], location[0] + entryView.width, location[1] + entryView.height)
                if (ancestor.visibleRect.width() == 0 || ancestor.visibleRect.height() == 0) {
                    ancestor.visibleRect.set(0, 0, Int.MAX_VALUE, Int.MAX_VALUE)
                }
            } else {
                val parent = entryView.parent as ViewGroup
                val visible = parent.getGlobalVisibleRect(ancestor.visibleRect)
                if (!visible) {
                    return null
                }
                ancestor.scrollX = parent.scrollX
                ancestor.scrollY = parent.scrollY
                ancestor.clipChildren = ViewCompatUtils.getClipChildren(parent)
            }

            ancestor.restrictedRect = ancestor.visibleRect
            ancestor.actualRect = ancestor.visibleRect

            val callback = object : IVTreeTraverseCallback {

                override fun onEnter(viewNode: View, layer: Int, visibleRect: Rect, actualRect: Rect, dataEntity: DataEntity?, isLogic: Boolean): Boolean {
                    // 视图本身可见性检测
                    if (viewNode.visibility != View.VISIBLE || dataEntity?.innerParams?.get(InnerKey.VIEW_LOGIC_VISIBLE) == false) {
                        visibleRect.set(0, 0, 0, 0)
                        return false
                    }

                    val selfRectF = detectionData.helperRectF
                    val width: Int = viewNode.right - viewNode.left
                    val height: Int = viewNode.bottom - viewNode.top
                    if (width < 0 || height < 0) {
                        visibleRect.set(0, 0, 0, 0)
                        return false
                    }
                    selfRectF.set(0f, 0f, width.toFloat(), height.toFloat())
                    // 根据matrix调整rect
                    val matrix = viewNode.matrix
                    val identity = matrix.isIdentity
                    if (!identity) {
                        matrix.mapRect(selfRectF)
                    }

                    // 计算视图的全局rect
                    // 获取父视图的信息
                    val parentInfo = if (isLogic) {
                        viewNode.getLocationOnScreen(detectionData.location)
                        detectionData.ancestorsInfo[0]
                    } else {
                        val parentInfoInner = detectionData.ancestorsInfo[layer - 1]
                        detectionData.location[0] = parentInfoInner.actualRect.left + viewNode.left - parentInfoInner.scrollX
                        detectionData.location[1] = parentInfoInner.actualRect.top + viewNode.top - parentInfoInner.scrollY
                        parentInfoInner
                    }

                    selfRectF.offset(detectionData.location[0].toFloat(), detectionData.location[1].toFloat())

                    var alphaActivity = false //针对透明 activity 展示dialog的场景，此时activity对应的decorView的宽高是0
                    if (layer == 1 && (selfRectF.width() == 0F || selfRectF.height() == 0F) && (entryView.parent == null || entryView.parent !is ViewGroup) && !isLogic) {
                        selfRectF.set(0f, 0f, Int.MAX_VALUE.toFloat(), Int.MAX_VALUE.toFloat())
                        alphaActivity = true
                    }

                    // 根据父亲提供的restrictedRect，限制一下自身区域
                    val selfInfo = detectionData.ancestorsInfo[layer]
                    val selfRect = selfInfo.visibleRect
                    selfRect.set(
                            selfRectF.left.toInt(),
                            selfRectF.top.toInt(),
                            selfRectF.right.toInt(),
                            selfRectF.bottom.toInt()
                    )
                    selfInfo.actualRect.set(selfRect)
                    if (!selfRect.intersect(parentInfo.restrictedRect) || selfRect.isEmpty) {
                        visibleRect.set(0, 0, 0, 0)
                        return false
                    }

                    //获取用户设置的可见区域的margin，也加入到计算的过程
                    val visibleMarginRect = dataEntity?.innerParams?.get(InnerKey.VIEW_VISIBLE_MARGIN) as? Rect?
                    actualRect.set(
                            selfInfo.actualRect.left + (visibleMarginRect?.left ?: 0),
                            selfInfo.actualRect.top + (visibleMarginRect?.top ?: 0),
                            selfInfo.actualRect.right - (visibleMarginRect?.right ?: 0),
                            selfInfo.actualRect.bottom - (visibleMarginRect?.bottom ?: 0)
                    )
                    visibleRect.set(
                            selfRect.left + (visibleMarginRect?.left ?: 0),
                            selfRect.top + (visibleMarginRect?.top ?: 0),
                            selfRect.right - (visibleMarginRect?.right ?: 0),
                            selfRect.bottom - (visibleMarginRect?.bottom ?: 0)
                    )
                    //这里把曝光比例的逻辑干掉了
/*                    val rate = dataEntity?.innerParams?.get(InnerKey.VIEW_EXPOSURE_MIN_RATE)
                    if (rate is Float) {
                        val actualArea = (viewNode.width - (visibleMarginRect?.left ?: 0) - (visibleMarginRect?.right ?: 0)) *
                                (viewNode.height - (visibleMarginRect?.top ?: 0) - (visibleMarginRect?.bottom ?: 0))
                        val exposureArea = visibleRect.height() * visibleRect.width()
                        val exposureRate = exposureArea * 1.0f / actualArea
                        if (exposureRate < rate) {
                            visibleRect.set(0, 0, 0, 0)
                            return false
                        }
                    }*/

                    if (viewNode !is ViewGroup) {
                        return false
                    }
                    // 如果是ViewGroup 需要继续探测子View 这里需要补充一下必要的信息
                    // 获取自己能提供给孩子的Rect
                    val restrictedRect = selfInfo.restrictedRect
                    if (!parentInfo.clipChildren) {
                        // 如果父亲的clipChildren是false 则孩子可能的rect区域可以和父亲一样大
                        restrictedRect.set(parentInfo.restrictedRect)
                    } else {
                        // 否则需要考虑自身的clipBounds和clipToPadding，取两者之间范围较小的一个
                        restrictedRect[0, 0, selfRect.right - selfRect.left] =
                                selfRect.bottom - selfRect.top
                        val clipBounds =
                                ViewCompat.getClipBounds(viewNode)
                        if (clipBounds != null && !restrictedRect.intersect(clipBounds)) {
                            // 如果clipBounds和自身区域不相交 说明子视图无法展示，就不继续了
                            return false
                        }
                        if (ViewCompatUtils.getClipToPadding(viewNode) && !restrictedRect.intersect(
                                        viewNode.getPaddingLeft(), viewNode.getPaddingTop(),
                                        viewNode.getWidth() - viewNode.getPaddingRight(),
                                        viewNode.getHeight() - viewNode.getPaddingBottom())) {
                            //这里本来是直接返回false的，但是由于透明的activity，里面可能有dialog，所以要做特殊的处理
                            //要设置restrictedRect为selfRect，并且可以继续往下遍历
                            if (!alphaActivity) {
                                // 同上，如果padding导致子视图无法显示，也不继续了
                                return false
                            }

                            restrictedRect[0, 0, selfRect.right - selfRect.left] =
                                    selfRect.bottom - selfRect.top
                        }
                        restrictedRect.offset(selfRect.left, selfRect.top)
                    }
                    restrictedRect.set(
                            restrictedRect.left + (visibleMarginRect?.left ?: 0),
                            restrictedRect.top + (visibleMarginRect?.top ?: 0),
                            restrictedRect.right - (visibleMarginRect?.right ?: 0),
                            restrictedRect.bottom - (visibleMarginRect?.bottom ?: 0)
                    )
                    selfInfo.scrollX = viewNode.getScrollX()
                    selfInfo.scrollY = viewNode.getScrollY()
                    selfInfo.clipChildren = ViewCompatUtils.getClipChildren(viewNode)
                    return true
                }

                override fun onLeave(node: View, layer: Int) {
                }
            }
            return if (isPartial) {
                VTreeTraverser.buildPartialViewTree(entryView, callback)
            } else {
                VTreeTraverser.buildViewTree(entryView, listDialogView, callback)
            }
        }
    }

}
