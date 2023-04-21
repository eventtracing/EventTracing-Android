package com.netease.cloudmusic.datareport.event

import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.getView

/**
 * web上报埋点监听
 */
object WebEventObserver {

    fun onWebEvent(eventType: WebEventType) {

        ReferManager.onWebViewEvent(eventType)

        EventDispatch.onEventNotifier(eventType, VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(getView(eventType.getTarget())))
    }

}