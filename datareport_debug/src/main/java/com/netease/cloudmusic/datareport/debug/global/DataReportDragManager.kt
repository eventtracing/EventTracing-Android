package com.netease.cloudmusic.datareport.debug.global

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.cloudmusic.datareport.debug.*
import com.netease.cloudmusic.datareport.report.exception.ExceptionObserver
import com.netease.cloudmusic.datareport.report.exception.ExceptionReporter
import com.netease.cloudmusic.datareport.report.exception.IErrorInfo
import org.json.JSONObject

object DataReportDragManager : View.OnClickListener, CompoundButton.OnCheckedChangeListener, ExceptionObserver {

    private val eventCallbackList = mutableListOf<DragEventListener>()

    private const val MASK_LEVEL = 0
    private const val ALERT_LEVEL = 0
    private const val FLOAT_LEVEL = 0

    private val context = DataReportDebugInstaller.mContext!!

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val dragFloatButtonLayoutParams = WindowManager.LayoutParams()

    private var isShowVTree = false

    private val debugButton: TextView
    private val errorButton: TextView
    private val errorNumText: TextView

    private val dashboardLayout: View
    private val markCheckbox: CheckBox
    private val dashboardClose: View
    private val dashboardListView: RecyclerView
    private val dashboardAdapter: DashboardAdapter

    private val exceptionList = mutableMapOf<String, MutableList<ErrorInfo>>()

    private val errorLayout : View
    private val errorTab: LinearLayout
    private val errorListView: RecyclerView
    private val errorCopyBtn : View
    private val errorCloseBtn: View
    private val errorInfoAdapter: ErrorInfoAdapter

    private val maskView: DataReportMaskView
    private val maskParams = WindowManager.LayoutParams()

    data class ErrorInfo(var errorInfo: IErrorInfo, var checked: Boolean)

    data class DashBoardItem(var title: Int, var checked: Boolean, var iconNormal: Int, var iconChecked: Int, val type: Int)
    data class ErrorTypeInfo(var key: String, var checked: Boolean, val id: Int)

    public const val SHOW_TYPE_EXPOSURE = 1
    public const val SHOW_TYPE_CLICK = 2
    public const val SHOW_TYPE_SLIDE = 3
    private val dashboardListData = arrayOf(DashBoardItem(R.string.data_report_export_pv, false, R.drawable.datareport_export_pv_close, R.drawable.datareport_export_pv_open, SHOW_TYPE_EXPOSURE),
        DashBoardItem(R.string.data_report_click_pv, false, R.drawable.datareport_click_pv_close, R.drawable.datareport_click_pv_open, SHOW_TYPE_CLICK),
        DashBoardItem(R.string.data_report_slide_pv, false, R.drawable.datareport_slide_pv_close, R.drawable.datareport_slide_pv_open, SHOW_TYPE_SLIDE))

    private val errorTypeList = arrayOf(ErrorTypeInfo("total", true, R.id.total), ErrorTypeInfo("EventKeyConflictWithEmbedded", false, R.id.EventKeyConflictWithEmbedded),
        ErrorTypeInfo("EventKeyInvalid", false, R.id.EventKeyInvalid), ErrorTypeInfo("LogicalMountEndlessLoop", false, R.id.LogicalMountEndlessLoop),
        ErrorTypeInfo("NodeNotUnique", false, R.id.NodeNotUnique), ErrorTypeInfo("NodeSPMNotUnique", false, R.id.NodeSPMNotUnique),
        ErrorTypeInfo("ParamConflictWithEmbedded", false, R.id.ParamConflictWithEmbedded), ErrorTypeInfo("PublicParamInvalid", false, R.id.PublicParamInvalid),
        ErrorTypeInfo("UserParamInvalid", false, R.id.UserParamInvalid)
    )
    private var checkedErrorType = errorTypeList[0]

    private val dragButton: DataReportDragFloatLayout = DataReportDragFloatLayout(context, windowManager, dragFloatButtonLayoutParams)

    init {
        dragButton.setBackgroundResource(R.drawable.datareport_float_button_bg)
        dragButton.elevation = 20f
        val floatButton = LayoutInflater.from(context).inflate(R.layout.datareport_float_button, dragButton, true)

        debugButton = floatButton.findViewById(R.id.debug_button)
        errorButton = floatButton.findViewById(R.id.error_button)
        errorNumText = floatButton.findViewById(R.id.error_num)
        errorButton.setText(R.string.data_float_error_text)
        debugButton.setOnClickListener(this)
        errorButton.setOnClickListener(this)

        //===================== dashBoard init =====================
        dashboardLayout = LayoutInflater.from(context).inflate(R.layout.datareport_dashboard, null, false)
        dashboardLayout.findViewById<View>(R.id.contextInfo).setOnClickListener(this)
        markCheckbox = dashboardLayout.findViewById(R.id.mark_checkbox)
        markCheckbox.setOnCheckedChangeListener(this)
        dashboardClose = dashboardLayout.findViewById(R.id.close)
        dashboardClose.setOnClickListener(this)
        dashboardListView = dashboardLayout.findViewById(R.id.dashboard_list)
        val layoutManage = GridLayoutManager(context, 4)
        dashboardListView.layoutManager = layoutManage
        dashboardAdapter = DashboardAdapter(dashboardListData)
        dashboardListView.adapter = dashboardAdapter
        //============================================================

        //===================== 错误信息看板 ==========================
        errorLayout = LayoutInflater.from(context).inflate(R.layout.datareport_error_layout, null, false)
        errorTab = errorLayout.findViewById(R.id.errorTab)
        errorListView = errorLayout.findViewById(R.id.error_list)
        errorCopyBtn = errorLayout.findViewById(R.id.error_copy)
        errorCopyBtn.setOnClickListener(this)
        errorCloseBtn = errorLayout.findViewById(R.id.error_close)
        errorCloseBtn.setOnClickListener(this)
        errorListView.layoutManager = LinearLayoutManager(context)
        errorInfoAdapter = ErrorInfoAdapter(exceptionList)
        errorListView.adapter = errorInfoAdapter
        //============================================================

        maskView = DataReportMaskView(context)
        maskView.setBackgroundResource(R.color.data_report_debug_mask)

        ExceptionReporter.addObserver(this)
        showDragFloatButton()
        updateDebugButtonText()
        updateErrorNumText()
    }

    fun registerEventCallback(listener: DragEventListener) {
        eventCallbackList.add(listener)
    }

    fun unRegisterEventCallback(listener: DragEventListener) {
        eventCallbackList.remove(listener)
    }

    /**
     * 展示悬浮按钮
     */
    private fun showDragFloatButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dragFloatButtonLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            dragFloatButtonLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION + FLOAT_LEVEL
        }
        dragFloatButtonLayoutParams.gravity = Gravity.LEFT or Gravity.CENTER;
        //设置flags 不然悬浮窗出来后整个屏幕都无法获取焦点，
        dragFloatButtonLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        dragFloatButtonLayoutParams.format = PixelFormat.RGBA_8888;
        dragFloatButtonLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        dragFloatButtonLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dragFloatButtonLayoutParams.x = 0;
        dragFloatButtonLayoutParams.y = 0;
        dragFloatButtonLayoutParams.flags = dragFloatButtonLayoutParams.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(dragButton, dragFloatButtonLayoutParams);
    }

    private fun changeCheckedList(position: Int) {
        if (position < dashboardListData.size) {
            if (dashboardListData[position].checked) {
                clearCheckedList()
            } else {
                clearCheckedList()
                dashboardListData[position].checked = true
                invokeListener { it.setEventNumType(dashboardListData[position].type) }
            }

            dashboardAdapter.notifyDataSetChanged()
            updateDebugButtonText()
        }
    }

    private fun clearCheckedList(){
        dashboardListData.iterator().forEach {
            it.checked = false
        }
        invokeListener { it.setEventNumType(0) }
    }

    class DashboardAdapter(private val dataSet: Array<DashBoardItem>) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.dashboard_list_item_text)
            val iconView: ImageView = view.findViewById(R.id.dashboard_list_item_icon)
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.datareport_dashboard_item, viewGroup, false)

            return ViewHolder(view)
        }
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val value = dataSet[position]
            viewHolder.view.setOnClickListener { changeCheckedList(position) }
            viewHolder.textView.setText(value.title)
            viewHolder.iconView.setImageResource(if(value.checked) value.iconChecked else value.iconNormal)
            viewHolder.textView.setTextColor(if(value.checked) viewHolder.textView.context.resources.getColor(
                R.color.data_report_dashboard_item_text_color) else viewHolder.textView.context.resources.getColor(
                R.color.data_report_float_button_text1))
        }
        override fun getItemCount() = dataSet.size
    }

    class ErrorInfoAdapter(private val dataSet: MutableMap<String, MutableList<ErrorInfo>>) : RecyclerView.Adapter<ErrorInfoAdapter.ViewHolder>() {

        inner class ViewHolder(val itemLayout: View) : RecyclerView.ViewHolder(itemLayout) {
            val numText = itemLayout.findViewById<TextView>(R.id.num_txt).apply { setTextColor(resources.getColor(
                R.color.data_report_float_button_text1)) }
            val infoText = itemLayout.findViewById<TextView>(R.id.info_txt).apply { setTextColor(resources.getColor(
                R.color.data_report_float_button_text1)) }
            val expandIcon = itemLayout.findViewById<ImageView>(R.id.expand_icon)
            val detailText = itemLayout.findViewById<TextView>(R.id.detail_txt).apply { setTextColor(resources.getColor(
                R.color.data_report_float_button_text1)) }
            val expandLayout = itemLayout.findViewById<ViewGroup>(R.id.expand_layout)
            val infoLayout = itemLayout.findViewById<ViewGroup>(R.id.info_layout)
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.datareport_float_item, viewGroup, false)

            return ViewHolder(view)
        }
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemLayout.setBackgroundResource(R.drawable.datareport_float_item_first_bg)
            val value = dataSet[checkedErrorType.key]?.get(position)?:return
            viewHolder.numText.text = (itemCount - position).toString()
            viewHolder.infoText.text = "[${value.errorInfo.key}][${value.errorInfo.code}]"
            viewHolder.detailText.text = JSONObject(value.errorInfo.getContent()).toString(4)
            viewHolder.expandLayout.visibility = View.GONE
            viewHolder.expandLayout.visibility = if(value.checked) View.VISIBLE else View.GONE
            viewHolder.expandIcon.setImageResource(if(value.checked) R.drawable.datareport_float_item_2 else R.drawable.datareport_float_item_1)
            viewHolder.infoLayout.setOnClickListener {
                if (viewHolder.expandLayout.visibility == View.GONE) {
                    viewHolder.expandLayout.visibility = View.VISIBLE
                    viewHolder.expandIcon.setImageResource(R.drawable.datareport_float_item_2)
                    value.checked = true
                } else {
                    viewHolder.expandLayout.visibility = View.GONE
                    viewHolder.expandIcon.setImageResource(R.drawable.datareport_float_item_1)
                    value.checked = false
                }
            }
        }

        override fun getItemCount() = dataSet[checkedErrorType.key]?.size ?: 0
    }

    /**
     * 展示控制面板
     */
    private fun showDashboard() {
        if (!dashboardLayout.isAttachedToWindow) {
            closeErrorLayout()
            val params = WindowManager.LayoutParams()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY + ALERT_LEVEL
            } else {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION + ALERT_LEVEL
            }
            params.gravity = Gravity.BOTTOM;
            //设置flags 不然悬浮窗出来后整个屏幕都无法获取焦点，
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.RGBA_8888;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(dashboardLayout, params);
        } else {
            closeDashboard()
        }
    }

    /**
     * 关闭控制面板
     */
    fun closeDashboard() {
        if (dashboardLayout.isAttachedToWindow) {
            windowManager.removeViewImmediate(dashboardLayout)
        }
    }

    /**
     * 展示错误面板
     */
    private fun showErrorLayout(){
        if (!errorLayout.isAttachedToWindow) {
            closeDashboard()
            val params = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY + ALERT_LEVEL
            } else {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION + ALERT_LEVEL
            }
            params.gravity = Gravity.BOTTOM;
            //设置flags 不然悬浮窗出来后整个屏幕都无法获取焦点，
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.RGBA_8888;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = context.resources.getDimension(R.dimen.datareport_error_layout_height).toInt()
            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(errorLayout, params);
        } else {
            closeErrorLayout()
        }
    }
    /**
     * 关闭错误面板
     */
    fun closeErrorLayout(){
        if (errorLayout.isAttachedToWindow) {
            windowManager.removeViewImmediate(errorLayout)
        }
    }

    /**
     * 上下文信息
     */
    private fun showContextInfo() {
        showVTree()
        maskView.showContext()
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.contextInfo -> {
                closeDashboard()
                showContextInfo()
            }
            R.id.debug_button -> {
                showDashboard()
            }
            R.id.error_button -> {
                showErrorLayout()
            }
            R.id.close -> {
                closeDashboard()
            }
            R.id.error_copy -> { //复制错误信息
                val checkedList = exceptionList[checkedErrorType.key]
                if (checkedList != null && checkedList.isNotEmpty()) {
                    val text =  JSONObject(checkedList[0].errorInfo.getContent()).toString(4)
                    val cm = v.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val mClipData = ClipData.newPlainText("Label", text)
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(v.context, R.string.data_copy_success, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.error_close -> {
                closeErrorLayout()
            }
        }
    }

    /**
     * 更新悬浮按钮的信息
     */
    private fun updateDebugButtonText() {
        dashboardListData.iterator().forEach {
            if (it.checked) {
                debugButton.setText(it.title)
                debugButton.setTextColor(context.resources.getColor(R.color.data_report_dashboard_item_text_color))
                return@forEach
            }
        }

        if (isShowVTree) {
            debugButton.setText(R.string.data_report_mark_view_debug_text)
            debugButton.setTextColor(context.resources.getColor(R.color.data_report_dashboard_item_text_color))
            return
        }
        debugButton.setText(R.string.data_report_mark_view_tool)
        debugButton.setTextColor(context.resources.getColor(R.color.data_report_float_button_text1))
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        isShowVTree = isChecked
        if (!isShowVTree) {
            hideVTree()
        } else {
            closeDashboard()
            showVTree()
            dashboardAdapter.notifyDataSetChanged()
        }
        updateDebugButtonText()
    }

    override fun onException(info: IErrorInfo) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            addErrorList(info, info.key)
            addErrorList(info, "total")
            updateErrorNumText()
        } else {
            dragButton.post{
                addErrorList(info, info.key)
                addErrorList(info, "total")
                updateErrorNumText()
            }
        }
    }

    private fun addErrorList(info: IErrorInfo, key: String) {
        var list = exceptionList[key]
        if (list == null) {
            list = mutableListOf()
            exceptionList[key] = list
        }
        list.add(0, ErrorInfo(info, false))
    }

    private val errorCheckedListener = object: CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (isChecked) {
                errorLayout.findViewById<CheckBox>(checkedErrorType.id).isChecked = false
                checkedErrorType = buttonView?.tag as ErrorTypeInfo
                errorInfoAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 在悬浮按钮上面更新错误数量
     */
    private fun updateErrorNumText() {
        errorTypeList.forEach {
            val list = exceptionList[it.key]
            val view = errorLayout.findViewById<CheckBox>(it.id)
            view.setOnCheckedChangeListener(errorCheckedListener)
            view.tag = it
            if (list == null || list.isEmpty()) {
                view.visibility = View.GONE
            } else {
                view.text = "${it.key}(${list.size})"
                view.visibility = View.VISIBLE
            }
        }

        val list = exceptionList[checkedErrorType.key]

        if (list == null || list.isEmpty()) {
            errorNumText.visibility = View.GONE
        } else {
            errorNumText.visibility = View.VISIBLE
            errorNumText.text = list.size.toString()
        }
        errorInfoAdapter.notifyDataSetChanged()
    }

    private fun invokeListener(block : (listener: DragEventListener) -> Unit) {
        eventCallbackList.forEach {
            block.invoke(it)
        }
    }

    /**
     * 展示虚拟树浮层信息
     */
    private fun showVTree() {
        if (maskView.isAttachedToWindow) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            maskParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            maskParams.type = WindowManager.LayoutParams.TYPE_APPLICATION + MASK_LEVEL
        }
        //设置flags 不然悬浮窗出来后整个屏幕都无法获取焦点，
        maskParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        maskParams.format = PixelFormat.RGBA_8888;
        maskParams.width = WindowManager.LayoutParams.MATCH_PARENT
        maskParams.height = WindowManager.LayoutParams.MATCH_PARENT
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(maskView, maskParams)

        windowManager.removeViewImmediate(dragButton)
        windowManager.addView(dragButton, dragFloatButtonLayoutParams)
    }

    /**
     * 隐藏虚拟树浮层信息
     */
    private fun hideVTree() {
        maskView.clear()
        if (maskView.isAttachedToWindow) {
            windowManager.removeViewImmediate(maskView)
        }
    }

}

interface DragEventListener {
    fun setEventNumType(type: Int)
}