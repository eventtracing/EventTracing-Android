package com.netease.datareport.demo.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.netease.cloudmusic.datareport.debug.ws.DataReportViewer
import com.netease.cloudmusic.datareport.eventtracing.DataReporter
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.datareport.demo.R
import com.netease.datareport.demo.web.WebViewActivity


class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        root.findViewById<Button>(R.id.open_web_view)?.apply {
            NodeBuilder.setElementId(this, "open_web")
            setOnClickListener {
                startActivity(Intent(context, WebViewActivity::class.java))
            }
        }
        val editView = root.findViewById<EditText>(R.id.ws_url)
        editView.setText("ws://intern.easyinsight-test.bdms.netease.com/process/realtime/app/89/1/1/1/1")
        root.findViewById<Button>(R.id.connect_ws)?.apply {
            NodeBuilder.setElementId(this, "connect_ws")
            setOnClickListener {
                DataReportViewer.connectWsServer(editView.text.toString().trim())
            }
        }
        NodeBuilder.getNodeBuilder(root).setPageId("home_page").params().putDynamicParams{
            mutableMapOf(Pair("firstKey", "firstParam")) as Map<String, Any>?
        }

        val debugUISwitch = root.findViewById<Switch>(R.id.open_debug)

        debugUISwitch.isChecked = DataReportInner.getInstance().configuration.isDebugUIEnable

        debugUISwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            DataReportInner.getInstance().configuration.setDebugUI(isChecked)
        })

        return root
    }
}
