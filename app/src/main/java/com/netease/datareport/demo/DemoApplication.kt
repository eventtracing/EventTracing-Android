package com.netease.datareport.demo

import android.app.Application
import android.util.Log
import android.webkit.WebView
import com.netease.cloudmusic.datareport.Configuration
import com.netease.cloudmusic.datareport.debug.ws.DataReportViewer
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.eventtracing.EventTracing
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.provider.IDynamicParamsProvider
import com.netease.cloudmusic.datareport.provider.IReferStrategy
import com.netease.cloudmusic.datareport.provider.IReporter
import org.json.JSONObject

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
        initDataReport(this)
    }

    private fun initDataReport(context: Application) {
        val hsReferList = HashSet<String>()
        hsReferList.add("FirstReferActivity")
        val config = Configuration.builder()
            .debugMode(true)
            .defaultDataCollectEnable(true)
            .setAOPScrollEnable(true)
            .setCustomEventPattern("^(_)[a-z]+(?:[-_][a-z]+)*$")
            .setCustomKeyPattern("^(s_){0,}[a-z][^\\W_]+[^\\W_]*(?:[-_][^\\W_]+)*$")
            .setGlobalKeyPattern("^(g_)[a-z][^\\W_]+[^\\W_]*(?:[-_][^\\W_]+)*$")
            .setVersionInfo("0.0.1")
            .provideDynamicParams(object: IDynamicParamsProvider {
                override fun setPublicDynamicParams(params: MutableMap<String, Any>?) {
                    params?.put("g_globalParamKey", "globalParam")
                }
                override fun setEventDynamicParams(event: String?, params: MutableMap<String, Any>?) {
                    if (event == EventKey.VIEW_CLICK) {
                        params?.put("_clickGlobalKey", "所有的点击事件都会带上这个参数")
                    }
                }
                override fun isActSeqIncrease(event: String?): Boolean {
                    return false
                }})
            .provideReporter(object: IReporter {
                override fun report(event: String, eventParams: MutableMap<String, Any>) {
                    Log.i("defaultReport", JSONObject(eventParams as Map<String, Any>).toString())
                    DataReportViewer.uploadLog(event, JSONObject(eventParams as Map<String, Any>))
                }
                override fun report(event: String, eventParams: JSONObject) {
                    Log.i("defaultReport", eventParams.toString())
                    DataReportViewer.uploadLog(event, eventParams)
                }
            })
            .provideReferStrategy(object : IReferStrategy {
                override fun buildScm(params: Map<String, Any>?): Pair<String, Boolean> {
                    return EventTracing.Companion.buildScmByEr(params)
                }
                override fun mutableReferLength(): Int { //mutablerefer 最多存5层
                    return 5
                }})
            .provideHsReferOidList(hsReferList)
            .build()
        DataReport.getInstance().init(DataReportInner.getInstance(), context,config)
    }

}