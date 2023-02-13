package com.netease.datareport.demo.dashboard

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.datareport.demo.R

/**
 * 基础能力展示
 */
class NormalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)
        NodeBuilder.setPageId(this, "NormalActivity") //给activity设置pageId
        findViewById<TextView>(R.id.test_text)?.let {
            NodeBuilder.getNodeBuilder(it).setElementId("test_text") //给text设置elementId
                .params()
                .putBICustomParam("testTextKey", "testTextParam")//设置节点的自定义静态参数
                .putDynamicParams{ mutableMapOf("testTextDynamicKey" to "testTextDynamicParam") as Map<String, Any>? } //设置节点的自定义动态参数
        }

        findViewById<Button>(R.id.test_button)?.let {
            it.setOnClickListener {
                Log.i("NormalActivity", "test button click")
            }
            NodeBuilder.getNodeBuilder(it).setElementId("test_button")
                .setReportPolicy(ReportPolicy.REPORT_POLICY_CLICK) //只上报点击
                .params()
                .addEventParamsCallback(arrayOf(EventKey.VIEW_CLICK)) { mutableMapOf("testClickKey" to "testClickParam") as Map<String, Any>? } //设置节点的事件动态参数
        }

        findViewById<Button>(R.id.test_button_second)?.let {
            NodeBuilder.setElementId(it, "test_button_second")
        }
    }

    fun testButtonClick(view: View) {
        Log.i("NormalActivity", "test button second click")
    }

}