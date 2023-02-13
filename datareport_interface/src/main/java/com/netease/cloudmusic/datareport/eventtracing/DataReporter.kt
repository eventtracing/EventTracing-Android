package com.netease.cloudmusic.datareport.eventtracing

import android.net.Uri
import com.netease.cloudmusic.datareport.event.EventConfig
import com.netease.cloudmusic.datareport.operator.DataReport

/**
 * 发送自定义时间的分装类
 */
class DataReporter private constructor(val action: String){

    private var mTarget: Any? = null
    private var isActSeqIncrease: Boolean = false
    private var isContainsRefer: Boolean = false
    private var params = mutableMapOf<String, Any>()
    private var referType: String? = null
    private var referSpm: String? = null
    private var referScm: String? = null
    private var referScmEr: Boolean? = null
    private var isGlobalDPRefer = false

    fun useForRefer(): DataReporter {
        isContainsRefer = true
        return this
    }

    fun useForActionSeq(): DataReporter {
        isActSeqIncrease = true
        return this
    }

    fun setParam(key: String, value: Any?): DataReporter {
        params[key] = value ?: ""
        return this
    }

    fun setParams(map: Map<String, Any?>): DataReporter {
        map.forEach { entry ->
            entry.value?.let {
                params[entry.key] = it
            }
        }
        return this
    }

    fun setParamBlock(block: ((map: MutableMap<String, Any?>) -> Unit)): DataReporter {
        val map = mutableMapOf<String, Any?>()
        block.invoke(map)
        setParams(map)
        return this
    }

    fun setTarget(target: Any?): DataReporter {
        this.mTarget = target
        return this
    }

    /**
     * 全局的report，也就是 view无关的时候，设置这个参数。type表示report的类型
     */
    fun setReferType(type: String?): DataReporter {
        referType = type
        return this
    }
    /**
     * 全局的report，也就是 view无关的时候，设置这个参数。
     */
    fun setReferSpm(spm: String?): DataReporter {
        referSpm = spm
        return this
    }
    /**
     * 全局的report，也就是 view无关的时候，设置这个参数。
     */
    fun setReferScm(scm: String?): DataReporter {
        if (scm != null) {
            val scmLength = 4
            val list = scm.split(":")
            if (list.size == scmLength) {
                val cid = list[0]
                if (EventTracing.checkEr(cid)) {
                    referScm = "${Uri.encode(cid)}:${list[1]}:${list[2]}:${list[3]}"
                    referScmEr = true
                    return this
                } else if (Uri.decode(cid) != cid) {
                    referScmEr = true
                }
            }
        }

        referScm = scm
        return this
    }

    /**
     * 通过多参数设置
     * 全局的report，也就是 view无关的时候，设置这个参数。
     */
    fun setReferScm(id: String?, type: String?, traceId: String?, trp: String?): DataReporter {
        val map = mutableMapOf<String, Any>(ParamBuilder.DATA_ID to (id
                ?: ""), ParamBuilder.DATA_TYPE to (type
                ?: ""), ParamBuilder.DATA_TRACE_ID to (traceId
                ?: ""), ParamBuilder.DATA_TRP to (trp ?: ""))

        val pair = EventTracing.buildScmByEr(map)
        referScm = pair.first
        referScmEr = pair.second
        return this
    }

    /**
     * 设置全局deeplink的refer
     */
    fun setGlobalDBRefer(): DataReporter {
        isGlobalDPRefer = true
        return this
    }

    fun report() {
        val view = mTarget
        val builder = EventConfig.Builder().setEventId(action).setParams(params).setIsActSeqIncrease(isActSeqIncrease).setIsContainsRefer(isContainsRefer)
        if (view != null) {
            builder.setTargetObj(view)
        }
        referType?.let { builder.setReferType(it) }
        referSpm?.let { builder.setReferSpm(it) }
        referScm?.let { builder.setReferScm(it) }
        if (referScmEr == true) {
            builder.setReferScmEr()
        }
        if (isGlobalDPRefer) {
            builder.setGlobalDPRefer()
        }

        DataReport.getInstance().reportEvent(builder.build())
    }

    companion object {
        const val ACTION_CLICK = "_ec"
        const val ACTION_PAGESTART = "_pv"
        const val ACTION_PAGEEND = "_pd"
        const val ACTION_VIEWSTART = "_ev"
        const val ACTION_SCROLL = "_es"
        const val ACTION_VIEWEND = "_ed"
        const val ACTION_PLAYSTART = "_plv"
        const val ACTION_PLAYEND = "_pld"
        const val ACTION_SEARCH_KEYWORD_CLIENT = "_skw"
        const val ACTION_KEYBOARD_SEARCH_KEYWORD_CLIENT = "_keyb_skw"

        fun actionBI(action: String): DataReporter {
            return DataReporter(action)
        }

        /**
         * 点击事件
         */
        fun clickBI(): DataReporter {
            return DataReporter(ACTION_CLICK).useForActionSeq().useForRefer()
        }

        /**
         * 滑动事件
         */
        fun scrollBI(): DataReporter {
            return DataReporter(ACTION_SCROLL)
        }

        /**
         * 页面开始曝光
         */
        fun pageStartBI(): DataReporter {
            return DataReporter(ACTION_PAGESTART)
        }

        /**
         * 页面结束曝光
         */
        fun pageEndBI(): DataReporter {
            return DataReporter(ACTION_PAGEEND)
        }

        /**
         * 元素开始曝光
         */
        fun viewStartBI(): DataReporter {
            return DataReporter(ACTION_VIEWSTART)
        }

        /**
         * 元素结束曝光
         */
        fun viewEndBI(): DataReporter {
            return DataReporter(ACTION_VIEWEND)
        }

        /**
         * 开始播放
         */
        fun playStartBI(): DataReporter {
            return DataReporter(ACTION_PLAYSTART)
        }

        /**
         * 结束播放
         */
        fun playEndBI(): DataReporter {
            return DataReporter(ACTION_PLAYEND)
        }

        /**
         * 搜索请求事件(不含分页加载)
         */
        fun searchKeywordClientBI(): DataReporter {
            return DataReporter(ACTION_SEARCH_KEYWORD_CLIENT)
        }
        /**
         * 软键盘点击搜索事件
         */
        fun keyboardSKWBI(): DataReporter {
            return DataReporter(ACTION_KEYBOARD_SEARCH_KEYWORD_CLIENT)
        }
    }
    
}