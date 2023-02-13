package com.netease.cloudmusic.datareport.vtree.exposure

import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode

abstract class DefaultExposureListener :
    IExposureListener {
    override fun onPageView(vTreeNode: VTreeNode, pgStepTemp: VTreeExposureManager.PgStepTemp) {
    }

    override fun onPageDisappear(vTreeNode: VTreeNode) {
    }

    override fun onElementView(vTreeNode: VTreeNode) {
    }

    override fun onElementDisappear(vTreeNode: VTreeNode) {
    }

}