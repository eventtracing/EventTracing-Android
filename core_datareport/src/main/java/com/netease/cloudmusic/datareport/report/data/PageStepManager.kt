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
        ReportUtils.getContext()?.let {
            return SPUtils.get(it, PAGE_STEP_KEY, 0)
        }
        return 0
    }

    fun setCurrentPageStep(step: Int) {
        ReportUtils.getContext()?.let {
            SPUtils.put(it, PAGE_STEP_KEY, step)
        }
    }

    fun getCurrentGlobalActSeq(): Int {
        ReportUtils.getContext()?.let {
            return SPUtils.get(it, GLOBAL_ACT_SEQ, 0)
        }
        return 0
    }

    fun setCurrentGlobalActSeq(step: Int) {
        ReportUtils.getContext()?.let {
            SPUtils.put(it, GLOBAL_ACT_SEQ, step)
        }
    }
    
    fun clear(){
        setCurrentPageStep(0)
        setCurrentGlobalActSeq(0)
    }
    
}