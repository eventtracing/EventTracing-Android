package com.netease.cloudmusic.datareport.event

import com.netease.cloudmusic.datareport.report.*
import java.lang.ref.WeakReference

/**
 * 上报事件的配置
 */
class EventConfig(builder: Builder) {

    /**
     * 事件id
     */
    private val eventId: String

    /**
     * Activity/Dialog/View类型，如果是null则不对相关类型上报，只单独上报事件和数据
     */
    private val targetObj: WeakReference<Any>?

    /**
     * 数据
     */
    private val params: Map<String, Any>

    /**
     * 是否计入refer
     */
    private val isContainsRefer: Boolean

    /**
     * 是否对actseq自增
     */
    private val isActSeqIncrease: Boolean

    private val isGlobalDPRefer: Boolean


    init {
        eventId = builder.eventId
        targetObj = builder.targetObj
        params = builder.params
        isContainsRefer = builder.isContainsRefer
        isActSeqIncrease = builder.isActSeqIncrease
        isGlobalDPRefer = builder.isGlobalDPRefer
    }

    fun getEventId(): String {
        return eventId
    }

    fun getTargetObj(): Any? {
        return targetObj?.get()
    }

    fun getParams(): Map<String, Any> {
        return params
    }

    fun isContainsRefer(): Boolean {
        return isContainsRefer
    }

    fun isActSeqIncrease(): Boolean {
        return isActSeqIncrease
    }

    fun isGlobalDPRefer(): Boolean {
        return isGlobalDPRefer
    }

    class Builder{
        internal var eventId: String = ""
        internal var targetObj: WeakReference<Any>? = null
        internal val params: MutableMap<String, Any> = mutableMapOf()
        internal var isContainsRefer: Boolean = false
        internal var isActSeqIncrease: Boolean = false
        internal var isGlobalDPRefer: Boolean = false

        fun setEventId(id: String): Builder {
            this.eventId = id
            return this
        }

        fun setTargetObj(obj: Any): Builder {
            this.targetObj = WeakReference(obj)
            return this
        }

        fun setParams(params: Map<String, Any>): Builder {
            this.params.putAll(params)
            return this
        }

        fun setIsContainsRefer(containsRefer: Boolean): Builder {
            this.isContainsRefer = containsRefer
            return this
        }
        fun setIsActSeqIncrease(actSeqIncrease: Boolean): Builder {
            this.isActSeqIncrease = actSeqIncrease
            return this
        }
        fun setReferType(type: String): Builder {
            this.params[REFER_TYPE] = type
            return this
        }
        fun setReferSpm(spm: String): Builder {
            this.params[REFER_SPM_KEY] = spm
            return this
        }
        fun setReferScm(scm: String): Builder {
            this.params[REFER_SCM_KEY] = scm
            return this
        }
        fun setReferScmEr(): Builder {
            this.params[FLAG_ER] = "1"
            return this
        }
        fun setGlobalDPRefer(): Builder {
            this.isGlobalDPRefer = true
            return this
        }

        fun build(): EventConfig {
            return EventConfig(this)
        }
    }

}