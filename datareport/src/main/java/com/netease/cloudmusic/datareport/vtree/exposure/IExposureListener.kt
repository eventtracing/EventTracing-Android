package com.netease.cloudmusic.datareport.vtree.exposure

import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

/**
 * 曝光监听
 */
interface IExposureListener {

    /**
     * 页面曝光
     */
    fun onPageView(vTreeNode: VTreeNode, pgStepTemp: VTreeExposureManager.PgStepTemp)

    /**
     * 页面反曝光
     */
    fun onPageDisappear(vTreeNode: VTreeNode)

    /**
     * 元素曝光
     */
    fun onElementView(vTreeNode: VTreeNode)

    /**
     * 元素反曝光
     */
    fun onElementDisappear(vTreeNode: VTreeNode)

}