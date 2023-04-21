package com.netease.cloudmusic.datareport

import android.view.View
import androidx.annotation.MainThread

/**
 * 虚拟树节点的 接口
 */
interface IVTreeNode {

    fun getParams(): Map<String, Any>?

    fun getExposureRate(): Float

    fun isPage(): Boolean

    fun isVirtualNode(): Boolean

    fun getInnerParam(key: String): Any?

    fun getNode(): View?

    /**
     * 获取在父控件的相对位置
     */
    fun getPos(): Int?

    fun getOid(): String

    fun getIdentifier(): String?

    fun getSpm(): String

    fun getScm(): String

    fun getScmByEr(): Pair<String, Boolean>

    fun getDebugHashCodeString(): String

    fun getUniqueCode(): Int

    //===================主线程执行===================
    @MainThread
    fun getSpmWithoutPos(): String
}