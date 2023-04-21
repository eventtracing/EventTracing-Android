package com.netease.cloudmusic.datareport.event

import android.app.Dialog
import android.view.View
import com.netease.cloudmusic.datareport.R
import com.netease.cloudmusic.datareport.data.DataEntity
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.utils.UIUtils
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.getOid
import com.netease.cloudmusic.datareport.vtree.getVTreeNode
import com.netease.cloudmusic.datareport.vtree.getView
import com.netease.cloudmusic.datareport.vtree.page.ViewContainerBinder

/**
 * 上报的事件类型的接口
 */
interface IEventType {

    companion object {
        /**
         * 根据View判断，是否忽略refer
         */
        fun isIgnoreRefer(view: Any?): Boolean {
            if (UIUtils.isMainThread()) {
                if (view is View && getOid(view).isNullOrEmpty()) {
                    if (ViewContainerBinder.getInstance().getBoundContainer(view.rootView) is Dialog) {
                        return true
                    }
                }

                val node = VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(getView(view))
                if (node != null) {
                    if (node.getInnerParam(InnerKey.VIEW_REFER_MUTE) == true) {
                        return true
                    }
                    var tempNode = node
                    while (tempNode?.parentNode != null) {
                        if (tempNode.getInnerParam(InnerKey.VIEW_IGNORE_REFER) == true) {
                            return true
                        }
                        tempNode = tempNode.parentNode
                    }
                } else {
                    var tempView = getView(view)
                    if ((tempView?.getTag(R.id.key_data_package) as? DataEntity)?.innerParams?.get(InnerKey.VIEW_REFER_MUTE) == true) {
                        return true
                    }
                    while (tempView != null) {
                        if ((tempView.getTag(R.id.key_data_package) as? DataEntity)?.innerParams?.get(InnerKey.VIEW_IGNORE_REFER) == true) {
                            return true
                        }
                        tempView = tempView.parent as? View
                    }
                }
            } else {
                val node = getVTreeNode(view)
                if (node != null) {
                    if (node.getInnerParam(InnerKey.VIEW_REFER_MUTE) == true) {
                        return true
                    }
                    var tempNode = node
                    while (tempNode?.parentNode != null) {
                        if (tempNode.getInnerParam(InnerKey.VIEW_IGNORE_REFER) == true) {
                            return true
                        }
                        tempNode = tempNode.parentNode
                    }
                }
            }

            return false
        }
    }

    /**
     * 事件类型的key
     */
    fun getEventType(): String

    /**
     * 事件对应的View
     */
    fun getTarget(): Any?

    /**
     * 事件参数
     */
    fun getParams(): Map<String, Any>

    /**
     * 是否考虑加入到归因
     */
    fun isContainsRefer(): Boolean

    /**
     * actseq是否自增
     */
    fun isActSeqIncrease(): Boolean

    /**
     * 是否计算到全局的refer中去
     */
    fun isGlobalDPRefer(): Boolean {
        return false
    }

}