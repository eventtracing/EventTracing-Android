package com.netease.datareport.demo.refer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.datareport.demo.R

class SecondReferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_first_refer)
        NodeBuilder.setPageId(this, "FirstReferActivity")
        findViewById<Button>(R.id.refer_button_1)?.let {
            it.visibility = View.GONE
        }
        findViewById<Button>(R.id.refer_button_2)?.let {
            it.visibility = View.GONE
        }
        findViewById<TextView>(R.id.sessid_text)?.text = DataReport.getInstance().sessionId
        findViewById<TextView>(R.id.sidrefer_text)?.text = DataReport.getInstance().sideRefer
        findViewById<TextView>(R.id.pgstep_text)?.text = DataReport.getInstance().currentPageStep.toString()
        findViewById<TextView>(R.id.multirefers_text)?.apply {
            postDelayed({
                this.text = DataReport.getInstance().multiRefer
            }, 1000)
        }    }
}