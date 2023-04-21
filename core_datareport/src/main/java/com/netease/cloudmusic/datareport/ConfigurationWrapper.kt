package com.netease.cloudmusic.datareport

import com.netease.cloudmusic.datareport.inner.DefaultDynamicParamsProvider
import com.netease.cloudmusic.datareport.inner.DefaultFormatter
import com.netease.cloudmusic.datareport.inner.DefaultLogger
import com.netease.cloudmusic.datareport.inner.DefaultReporter
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.cloudmusic.datareport.provider.*
import com.netease.cloudmusic.datareport.utils.BaseUtils
import com.netease.cloudmusic.datareport.utils.SPUtils
import com.netease.cloudmusic.datareport.utils.SPUtils.DATA_REPORT_DEBUG_UI
import java.util.HashSet
import java.util.regex.Pattern

class ConfigurationWrapper(private val configuration: Configuration) : Configuration() {

    override fun isAopScrollEnable(): Boolean {
        return configuration.isAopScrollEnable
    }

    override fun isElementExposureEnd(): Boolean {
        return configuration.isElementExposureEnd
    }

    override fun setDebugUI(isDebug: Boolean) {
        SPUtils.put(DATA_REPORT_DEBUG_UI, isDebug)
    }

    override fun isDebugUIEnable(): Boolean {
        return SPUtils.get(DATA_REPORT_DEBUG_UI, false)
    }

    override fun getReferStrategy(): IReferStrategy {
        return configuration.referStrategy
    }

    override fun getPatternCustomEvent(): Pattern? {
        return configuration.patternCustomEvent
    }

    override fun getPatternGlobalKey(): Pattern? {
        return configuration.patternGlobalKey
    }

    override fun getPatternCustomKey(): Pattern? {
        return configuration.patternCustomKey
    }

    override fun getExposureMinTime(): Long? {
        return configuration.exposureMinTime
    }

    override fun setDataCollectEnable(dataCollectEnable: Boolean) {
        configuration.isDataCollectEnable = dataCollectEnable
    }

    override fun getHsReferOidList(): HashSet<String> {
        return configuration.hsReferOidList
    }

    override fun isDataCollectEnable(): Boolean {
        return configuration.isDataCollectEnable
    }

    override fun isDebugMode(): Boolean {
        return configuration.isDebugMode
    }

    override fun getReportPolicy(): ReportPolicy {
        return configuration.reportPolicy
    }

    override fun getLogger(): ILogger {
        return BaseUtils.nullAs(configuration.logger, DefaultLogger.getInstance())
    }

    override fun getFormatter(): IFormatter {
        return BaseUtils.nullAs(configuration.formatter, DefaultFormatter.getInstance())
    }

    override fun getReporter(): IReporter {
        return BaseUtils.nullAs(configuration.reporter, DefaultReporter.getInstance())
    }

    override fun getDynamicParamsProvider(): IDynamicParamsProvider {
        return BaseUtils.nullAs(configuration.dynamicParamsProvider, DefaultDynamicParamsProvider.getInstance())
    }

    override fun getSyncDynamicParamsProvider(): IDynamicParamsProvider {
        return BaseUtils.nullAs(configuration.syncDynamicParamsProvider, DefaultDynamicParamsProvider.getInstance())
    }

    override fun getAppVisitProvider(): IAppVisitProvider? {
        return configuration.appVisitProvider
    }

    override fun toString(): String {
        return configuration.toString()
    }

    override fun getVersionInfo(): String {
        return configuration.versionInfo
    }

}