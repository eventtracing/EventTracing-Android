package com.netease.datareport.demo.refer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.eventtracing.DataReporter
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.datareport.demo.R

class FirstReferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_refer)
        NodeBuilder.setPageId(this, "FirstReferActivity")
        findViewById<Button>(R.id.refer_button_1)?.let {
            it.setOnClickListener {
                startActivity(Intent(this, SecondReferActivity::class.java))
            }
            NodeBuilder.getNodeBuilder(it).setElementId("referButton").params().putBICustomParam("s_id", "first_btn")
                .putBIPosition(1) //设置位置信息，当同一个层级node有相同的oid时，必须要设置position
        }
        findViewById<Button>(R.id.refer_button_2)?.let {
            it.setOnClickListener { _->
                DataReporter.clickBI().setTarget(it).useForRefer() //链路追踪
                    .report() //自定义上报
                startActivity(Intent(this, SecondReferActivity::class.java))
            }

            NodeBuilder.getNodeBuilder(it).setElementId("referButton")
                .setReportPolicy(ReportPolicy.REPORT_POLICY_EXPOSURE) //禁止自动上报点击，通过自定义来上报
                .params().putBICustomParam("s_id", "second_btn")
                .putBIPosition(2) //设置位置信息，当同一个层级node有相同的oid时，必须要设置position
        }
        findViewById<TextView>(R.id.sessid_text)?.text = DataReport.getInstance().sessionId
        findViewById<TextView>(R.id.sidrefer_text)?.text = DataReport.getInstance().sideRefer
        findViewById<TextView>(R.id.pgstep_text)?.text = DataReport.getInstance().currentPageStep.toString()

        findViewById<TextView>(R.id.multirefers_text)?.apply {
            postDelayed({
                        this.text = DataReport.getInstance().multiRefer
            }, 1000)
        }

    }
}