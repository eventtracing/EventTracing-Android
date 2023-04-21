package com.netease.cloudmusic.datareport.debug.tree

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.netease.cloudmusic.datareport.app.AppEventReporter
import com.netease.cloudmusic.datareport.debug.R
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.cloudmusic.datareport.report.ExposureEventReport
import com.netease.cloudmusic.datareport.report.data.PageContext
import com.netease.cloudmusic.datareport.report.data.PageContextManager
import com.netease.cloudmusic.datareport.report.refer.ReferManager
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.isRootPage
import java.lang.StringBuilder

/**
 * 虚拟树的节点信息的看板
 */
class VTreeInfoBoard(private val targetView: ViewGroup): View.OnClickListener {

    companion object {
        private val FLOAT_ITEM_BG = arrayOf(
            R.drawable.datareport_float_item_first_bg,
            R.drawable.datareport_float_item_second_bg,
            R.drawable.datareport_float_item_third_bg,
            R.drawable.datareport_float_item_fourth_bg
        )
        private val FLOAT_ITEM_TEXT_COLOR = arrayOf(
            R.color.data_report_float_text_first,
            R.color.data_report_float_text_second,
            R.color.data_report_float_text_third,
            R.color.data_report_float_text_fourth
        )
    }

    private val layoutInflater = LayoutInflater.from(targetView.context)
    private val infoFloat : View =
        layoutInflater.inflate(R.layout.datareport_float_layout, null)
    private val spmTextView: TextView
    private val contentLayout: LinearLayout

    init {
        infoFloat.setOnTouchListener { _, _ -> true }
        spmTextView = infoFloat.findViewById(R.id.spm_text)
        infoFloat.findViewById<View>(R.id.close)?.setOnClickListener(this)
        infoFloat.findViewById<View>(R.id.copyBtn)?.setOnClickListener(this)
        contentLayout = infoFloat.findViewById(R.id.content_layout)
    }

    fun showContext() {
        if (infoFloat.parent == null) {
            targetView.addView(infoFloat, FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, targetView.context.resources.getDimension(
                R.dimen.datareport_float_height
            ).toInt()).apply {
                val margin = targetView.context.resources.getDimension(R.dimen.datareport_float_margin).toInt()
                this.leftMargin = margin
                this.rightMargin = margin
                this.gravity = Gravity.CENTER
            })
        }
        spmTextView.text = "上下文"
        contentLayout.removeAllViews()
        layer = 1
        clipboardBuilder.clear()
        buildContextItem("_multirefers", ReferManager.getMutableRefer(), 1)
        buildContextItem("_hsrefer", ReferManager.getHsRefer(), 2)
        buildContextItem("_actseq", ExposureEventReport.getActionSeq(VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(AppEventReporter.getInstance().currentActivity.window.decorView)).toString(), 3)
        buildContextItem("_sessid", AppEventReporter.getInstance().currentSessionId, 4)
    }

    fun updateView(node: VTreeNode?, view: View?){
        if (infoFloat.parent == null) {
            targetView.addView(infoFloat, FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, targetView.context.resources.getDimension(
                R.dimen.datareport_float_height
            ).toInt()).apply {
                val margin = targetView.context.resources.getDimension(R.dimen.datareport_float_margin).toInt()
                this.leftMargin = margin
                this.rightMargin = margin
                this.gravity = Gravity.CENTER
            })
        }
        spmTextView.text = "对象链路  ${VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(view)?.getSpm()}"
        contentLayout.removeAllViews()
        layer = 1
        clipboardBuilder.clear()
        node?.let {
            traverse(it)
        }
    }

    private val clipboardBuilder = StringBuilder()

    private var layer: Int = 1

    private fun traverse(node: VTreeNode) {
        val parentNode = node.parentNode
        if (parentNode != null) {
            traverse(parentNode)
            buildVTreeNodeItem(node, layer++)
        }
    }

    private fun buildVTreeNodeItem(node: VTreeNode, layer: Int) {
        val itemLayout = layoutInflater.inflate(R.layout.datareport_float_item, null)
        var index = layer - 1
        index = if(index < FLOAT_ITEM_BG.size) index else FLOAT_ITEM_BG.size - 1
        itemLayout.setBackgroundResource(FLOAT_ITEM_BG[index])
        val numText = itemLayout.findViewById<TextView>(R.id.num_txt).apply { setTextColor(resources.getColor(
            FLOAT_ITEM_TEXT_COLOR[index])) }
        val infoText = itemLayout.findViewById<TextView>(R.id.info_txt).apply { setTextColor(resources.getColor(
            FLOAT_ITEM_TEXT_COLOR[index])) }
        val expandIcon = itemLayout.findViewById<ImageView>(R.id.expand_icon)
        val detailText = itemLayout.findViewById<TextView>(R.id.detail_txt).apply { setTextColor(resources.getColor(
            FLOAT_ITEM_TEXT_COLOR[index])) }
        val expandLayout = itemLayout.findViewById<ViewGroup>(R.id.expand_layout)
        val infoLayout = itemLayout.findViewById<ViewGroup>(R.id.info_layout)
        numText.text = layer.toString()
        infoText.text = "[${if(node.isPage()) "p" else "e"}]${node.getOid()}"
        detailText.text = getDetailInfo(node)
        clipboardBuilder.append(infoText.text.toString()).append("\n")
            .append(detailText.text.toString()).append("\n")
        expandLayout.visibility = View.GONE
        infoLayout.setOnClickListener {
            if (expandLayout.visibility == View.GONE) {
                expandLayout.visibility = View.VISIBLE
                expandIcon.setImageResource(R.drawable.datareport_float_item_2)
            } else {
                expandLayout.visibility = View.GONE
                expandIcon.setImageResource(R.drawable.datareport_float_item_1)
            }
        }

        contentLayout.addView(itemLayout, 0, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, contentLayout.context.resources.getDimension(R.dimen.datareport_float_item_margin).toInt())
        })
    }

    private fun buildContextItem(title: String, detail: String, layer: Int) {
        val itemLayout = layoutInflater.inflate(R.layout.datareport_float_item, null)
        var index = layer - 1
        index = if(index < FLOAT_ITEM_BG.size) index else FLOAT_ITEM_BG.size - 1
        itemLayout.setBackgroundResource(FLOAT_ITEM_BG[index])
        val numText = itemLayout.findViewById<TextView>(R.id.num_txt).apply { setTextColor(resources.getColor(
            FLOAT_ITEM_TEXT_COLOR[index])) }
        val infoText = itemLayout.findViewById<TextView>(R.id.info_txt).apply { setTextColor(resources.getColor(
            FLOAT_ITEM_TEXT_COLOR[index])) }
        val expandIcon = itemLayout.findViewById<ImageView>(R.id.expand_icon)
        val detailText = itemLayout.findViewById<TextView>(R.id.detail_txt).apply { setTextColor(resources.getColor(
            FLOAT_ITEM_TEXT_COLOR[index])) }
        val expandLayout = itemLayout.findViewById<ViewGroup>(R.id.expand_layout)
        val infoLayout = itemLayout.findViewById<ViewGroup>(R.id.info_layout)
        numText.text = layer.toString()
        infoText.text = title
        detailText.text = detail
        clipboardBuilder.append(infoText.text.toString()).append("\n")
            .append(detailText.text.toString()).append("\n")
        expandLayout.visibility = View.GONE
        infoLayout.setOnClickListener {
            if (expandLayout.visibility == View.GONE) {
                expandLayout.visibility = View.VISIBLE
                expandIcon.setImageResource(R.drawable.datareport_float_item_2)
            } else {
                expandLayout.visibility = View.GONE
                expandIcon.setImageResource(R.drawable.datareport_float_item_1)
            }
        }

        contentLayout.addView(itemLayout, 0, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, contentLayout.context.resources.getDimension(R.dimen.datareport_float_item_margin).toInt())
        })
    }

    private fun getDetailInfo(node: VTreeNode): String{
        val sb = StringBuilder()
        if(node.getInnerParam(InnerKey.VIEW_VIRTUAL_PARENT_NODE) != null){
            sb.append("parent_virtual: true\n\n")
        }
        sb.append("visible_rect: ").append(node.visibleRect).append("\n\n")
        sb.append("actual_rect: ").append(node.actualRect).append("\n\n")
        sb.append("exposure_rate: ").append(node.getExposureRate()).append("\n\n")
        val rect = node.getInnerParam(InnerKey.VIEW_VISIBLE_MARGIN)
        if (rect != null && rect is Rect) {
            sb.append("insets: ").append(rect.toString()).append("\n\n")
        }
        if (node.isPage()) {
            sb.append("page: ").append("${if(isRootPage(node)) "rootpage" else "subpage"}").append("\n\n")
        }
        if(node.getInnerParam(InnerKey.LOGIC_PARENT) != null || node.getInnerParam(InnerKey.VIEW_ALERT_FLAG) != null){
            sb.append("logical_mount: true\n\n")
        }
        val time = node.getInnerParam(InnerKey.VIEW_EXPOSURE_MIN_TIME)
        val rate = node.getInnerParam(InnerKey.VIEW_EXPOSURE_MIN_RATE)
        if (time != null || rate != null) {
            sb.append("threshold: ").append("[${time}ms] && ").append("[${rate}]").append("\n\n")
        }
        sb.append("disable_buildin_log: ")
        val policy = node.getInnerParam(InnerKey.VIEW_REPORT_POLICY)
        val exposureEnd = node.getInnerParam(InnerKey.VIEW_ELEMENT_EXPOSURE_END)
        if (policy != null && policy is ReportPolicy) {
            if (policy.reportExposure) {
                sb.append("[impress]")
            }
            if (policy.reportClick) {
                sb.append("[click]")
            }
        } else {
            sb.append("[impress]")
            sb.append("[click]")
        }
        if (node.isPage() || exposureEnd == true) {
            sb.append("[impressend]")
        }
        sb.append("\n\n")
        val position = node.getInnerParam(InnerKey.VIEW_POSITION)
        if (position != null) {
            sb.append("position: ").append(position).append("\n\n")
        }
        val toOid = node.getInnerParam(InnerKey.VIEW_TO_OID)
        if (toOid != null) {
            sb.append("toids: ").append(toOid).append("\n\n")
        }
        node.getParams()?.forEach {
            sb.append(it.key).append(" : ").append(it.value).append("\n\n")
        }
        sb.append("spm: ").append(node.getSpm()).append("\n\n")
        sb.append("scm: ").append(node.getScm()).append("\n\n")
        if (node.isPage()) {
            sb.append("pgstep: ").append((PageContextManager.getInstance().get(node.hashCode()) as PageContext).pageStep).append("\n\n")
        }
        if (isRootPage(node)) {
            val context = PageContextManager.getInstance().get(node.hashCode()) as PageContext
            sb.append("actseq: ").append(context.actSeq).append("\n\n")
            sb.append("pgrefer: ").append(context.pgRefer).append("\n\n")
            sb.append("psrefer: ").append(context.psRefer).append("\n\n")
        }

        return sb.substring(0, sb.length - 1).toString()
    }


    fun clearView(){
        targetView.removeView(infoFloat)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.close -> {clearView()}
            R.id.copyBtn -> {
                val cm = v.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val mClipData = ClipData.newPlainText("Label", clipboardBuilder.toString())
                cm.setPrimaryClip(mClipData);
                Toast.makeText(v.context, R.string.data_copy_success, Toast.LENGTH_SHORT).show()
            }
        }
    }

}