package com.netease.datareport.demo.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.datareport.demo.refer.FirstReferActivity
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider
import com.netease.datareport.demo.R

class DashboardFragment : Fragment() {

    companion object{
        private const val TAG = "DashboardFragment"
    }

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var dynamicParamsProvider: IViewDynamicParamsProvider

    private var dynamicNum = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val map = mapOf<String, Any>(Pair("firstKey", "firstParam"), Pair("secondKey", "secondParam"))
        dynamicParamsProvider = IViewDynamicParamsProvider { mutableMapOf<String, Any>(Pair("dynamicKey", ++dynamicNum)) }
        NodeBuilder.getNodeBuilder(root).setPageId("DashboardFragment").params().putBICustomParams(map)
            .putDynamicParams(dynamicParamsProvider)

        root.findViewById<Button>(R.id.normal)?.let {
            it.setOnClickListener {
                startActivity(Intent(context, NormalActivity::class.java))
            }
            NodeBuilder.setElementId(it, "normalBtn")
        }
        root.findViewById<Button>(R.id.logical_visible_area)?.let {
            it.setOnClickListener {
                startActivity(Intent(context, LogicalAndVisibleActivity::class.java))
            }
            NodeBuilder.setElementId(it, "logicalVisibleBtn")
        }
        root.findViewById<Button>(R.id.exposure)?.let {
            it.setOnClickListener {
                startActivity(Intent(context, ExposureActivity::class.java))
            }
            NodeBuilder.setElementId(it, "exposureBtn")
            NodeBuilder.getNodeBuilder(it).setElementId("exposureBtn")
        }
        root.findViewById<Button>(R.id.customEvent)?.let {
            it.setOnClickListener {
                startActivity(Intent(context, FirstReferActivity::class.java))
            }
            NodeBuilder.setElementId(it, "customEventBtn")
        }

        return root
    }
}
