package com.netease.cloudmusic.datareport.policy

/**
 * 设置一个虚拟父节点的配置
 */
class VirtualParentConfig private constructor(
    private val params: Map<String, Any>?, //虚拟父节点的参数
    private val position: Int?, //虚拟父节点的pos
    private val reportPolicy: ReportPolicy?, //虚拟父节点的上报策略
    private val exposureEndEnable: Boolean? //虚拟父节点是否上报曝光结束
) {

    fun getParams(): Map<String, Any>? {
        return params
    }

    fun getPosition(): Int?{
        return position
    }
    fun getReportPolicy(): ReportPolicy? {
        return reportPolicy
    }
    fun getExposureEndEnable():Boolean? {
        return exposureEndEnable
    }

    class Builder {
        private var params: Map<String, Any>? = null
        private var position: Int? = null
        private var reportPolicy: ReportPolicy? = null
        private var exposureEndEnable: Boolean? = null

        fun setParams(params: Map<String, Any>): Builder {
            this.params = HashMap(params)
            return this
        }
        fun setPosition(position: Int): Builder {
            this.position = position
            return this
        }
        fun setReportPolicy(reportPolicy: ReportPolicy): Builder {
            this.reportPolicy = reportPolicy
            return this
        }
        fun setExposureEndEnable(exposureEndEnable: Boolean): Builder {
            this.exposureEndEnable = exposureEndEnable
            return this
        }
        fun build(): VirtualParentConfig {
            return VirtualParentConfig(
                params,
                position,
                reportPolicy,
                exposureEndEnable
            )
        }

    }

}