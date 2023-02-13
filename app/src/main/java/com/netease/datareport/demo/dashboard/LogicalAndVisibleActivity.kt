package com.netease.datareport.demo.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.policy.VirtualParentConfig
import com.netease.datareport.demo.R

class LogicalAndVisibleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logical_visible)
        NodeBuilder.setPageId(this, "LogicalAndVisibleActivity")
        NodeBuilder.setPageId(findViewById<LinearLayout>(R.id.top_page), "topPage")
        NodeBuilder.setPageId(findViewById<LinearLayout>(R.id.bottom_page), "bottomPage")
        findViewById<TextView>(R.id.logical_invisible)?.let {
            NodeBuilder.getNodeBuilder(it).setElementId("textInVisible")
                .setLogicVisible(false) //设置逻辑不可见
        }
        findViewById<TextView>(R.id.virtual_parent_t1)?.let {
            NodeBuilder.setElementId(it, "virtualParentT1")
            setLogicParent(it)
        }
        findViewById<TextView>(R.id.virtual_parent_t2)?.let {
            NodeBuilder.setElementId(it, "virtualParentT2")
            setLogicParent(it)
        }
        findViewById<TextView>(R.id.invisible_text)?.let {
            NodeBuilder.setElementId(it,"invisibleText") //会被 bottomPage遮挡，所以不会曝光
        }

        findViewById<Button>(R.id.open_dialog)?.let {
            it.setOnClickListener {
                AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("弹窗自动挂载").setMessage("他会把后面的element进行遮挡").show().apply {
                    NodeBuilder.setPageId(this, "testDialog") //弹窗会被自动到根节点
                }
            }
            NodeBuilder.getNodeBuilder(it).setElementId("openDialogButton")
                .setElementExposureEnd(true)
        }

        findViewById<TextView>(R.id.logical_text)?.let {
            NodeBuilder.getNodeBuilder(it).setElementId("logicalText")
                .setLogicParent(findViewById<LinearLayout>(R.id.top_page)) //把logicalText逻辑挂载到top_page
        }

        findViewById<TextView>(R.id.logical_text_auto)?.let {
            NodeBuilder.getNodeBuilder(it).setElementId("logicalTextAuto")
                .setViewAsAlert(true, 1) //设置自动逻辑挂载，他会被挂载到根节点
        }

    }

    /**
     * 设置虚拟父节点
     */
    private fun setLogicParent(view: View) {
        NodeBuilder.getNodeBuilder(view).setVirtualParentNode(
            "virtualParent", //虚拟父节点的elementId
            "virtualParent", //判断虚拟父节点的唯一值，正常节点通过View的hashCode来判断唯一值，虚拟父节点需要手动指定。多个node是否挂载到一个虚拟父节点上面主要取决于identifier是否相同
            VirtualParentConfig.Builder().setParams(mapOf("virtualParentKey" to "virtualParentParam")) //设置虚拟父节点的参数
                .setExposureEndEnable(true) //设置虚拟父节点是否要上报曝光结束
                .build())
    }
}