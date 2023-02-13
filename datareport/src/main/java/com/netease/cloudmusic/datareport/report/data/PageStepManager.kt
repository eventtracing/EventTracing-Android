package com.netease.cloudmusic.datareport.report.data

import com.netease.cloudmusic.datareport.utils.ReportUtils
import com.netease.cloudmusic.datareport.utils.SPUtils

/**
 * 页面深度，只有在冷启动的时候被初始化，后面会一直往上加
 */
object PageStepManager {

    private const val GLOBAL_ACT_SEQ = "global_act_seq_id"

    //页面深度的
    private const val PAGE_STEP_KEY = "page_step_id"

    fun getCurrentPageStep(): Int {
        return SPUtils.get(ReportUtils.getContext(), PAGE_STEP_KEY, 0)
    }

    fun setCurrentPageStep(step: Int) {
        SPUtils.put(ReportUtils.getContext(), PAGE_STEP_KEY, step)
    }

    fun getCurrentGlobalActSeq(): Int {
        return SPUtils.get(ReportUtils.getContext(), GLOBAL_ACT_SEQ, 0)
    }

    fun setCurrentGlobalActSeq(step: Int) {
        SPUtils.put(ReportUtils.getContext(), GLOBAL_ACT_SEQ, step)
    }
    
    fun clear(){
        setCurrentPageStep(0)
        setCurrentGlobalActSeq(0)
    }
    
}