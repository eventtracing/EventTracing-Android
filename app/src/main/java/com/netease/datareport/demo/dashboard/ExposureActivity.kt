package com.netease.datareport.demo.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netease.cloudmusic.datareport.eventtracing.EventTracing
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.datareport.demo.R

class ExposureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exposure)
        NodeBuilder.setPageId(this, "ExposureActivity")
        NodeBuilder.setPageId(findViewById<LinearLayout>(R.id.mask_layout), "maskLayout")
        findViewById<Button>(R.id.re_exposure_view)?.let {
            it.setOnClickListener { _ ->
                EventTracing.reExposureView(it)
            }
            NodeBuilder.getNodeBuilder(it).setElementId("reExposureView").setElementExposureEnd(true)//支持元素的曝光结束
        }

        findViewById<TextView>(R.id.exposure_end_view)?.let {
            NodeBuilder.getNodeBuilder(it).setElementId("exposureEndView")
                .setElementExposureEnd(true) //设置支持曝光结束，默认是不会上报曝光结束的
        }

        findViewById<Button>(R.id.exposure_rate_view)?.let {
            it.setOnClickListener { _ ->
                it.visibility = View.GONE //在曝光结束的上报中会打印最大的可见的rate
                EventTracing.reExposureView(it) //这里需要重新构建视图树，设置view的可见性并没有被aop
            }
            NodeBuilder.getNodeBuilder(it).setElementId("exposureRateView")
                .setElementExposureEnd(true) //设置支持曝光结束，默认是不会上报曝光结束的
        }

        findViewById<Button>(R.id.exposure_duration_view)?.let {
            it.setOnClickListener { _ ->
                it.visibility = View.GONE //在曝光结束的上报中会打印曝光的时长
                EventTracing.reExposureView(it) //这里需要重新构建视图树，设置view的可见性并没有被aop
            }
            NodeBuilder.getNodeBuilder(it).setElementId("exposureDurationView")
                .setElementExposureEnd(true) //设置支持曝光结束，默认是不会上报曝光结束的
        }
    }
}