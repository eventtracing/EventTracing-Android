package com.netease.datareport.demo.dashboard

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.netease.cloudmusic.datareport.event.EventKey
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.eventtracing.DataReporter
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.datareport.demo.R

class CustomEventActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_event)

        NodeBuilder.setPageId(this, "CustomEventActivity")

        findViewById<Button>(R.id.custom_node)?.let {
            it.setOnClickListener { _ ->
                DataReporter.actionBI(EventKey.VIEW_CLICK).setTarget(it).setParam("customNodeKey", "customNodeParam").report() //有节点的自定义事件，并且不会链路追踪
            }
            NodeBuilder.getNodeBuilder(it).setElementId("customNode")
                .setReportPolicy(ReportPolicy.REPORT_POLICY_EXPOSURE) //设置只曝光，不会触发点击上报
        }
        findViewById<Button>(R.id.custom_no_node)?.let {
            it.setOnClickListener { _ ->
                DataReporter.actionBI(EventKey.VIEW_CLICK).setParam("customNodeKey", "customNodeParam").report()
            }
            NodeBuilder.getNodeBuilder(it).setElementId("customNoNode").setReportPolicy(ReportPolicy.REPORT_POLICY_EXPOSURE) //设置只曝光，不会触发点击上报
        }
        findViewById<Button>(R.id.custom_refer)?.let {
            it.setOnClickListener { _ ->
                DataReporter.actionBI(EventKey.VIEW_CLICK).setParam("customNodeKey", "customNodeParam")
                    .useForRefer().setTarget(it).report() //有节点的自定义事件，并且参与链路追踪
            }
            NodeBuilder.getNodeBuilder(it).setElementId("customRefer")
                .setReportPolicy(ReportPolicy.REPORT_POLICY_EXPOSURE) //设置只曝光，不会触发点击上报
        }
    }


}