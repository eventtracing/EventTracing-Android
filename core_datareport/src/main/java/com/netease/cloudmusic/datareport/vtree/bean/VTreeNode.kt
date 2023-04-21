package com.netease.cloudmusic.datareport.vtree.bean

import android.graphics.Rect
import android.view.View
import androidx.annotation.MainThread
import com.netease.cloudmusic.datareport.IVTreeNode
import com.netease.cloudmusic.datareport.data.DataEntity
import com.netease.cloudmusic.datareport.data.ReusablePool
import com.netease.cloudmusic.datareport.event.EventDispatch
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider
import com.netease.cloudmusic.datareport.report.data.IContext
import com.netease.cloudmusic.datareport.utils.UIUtils
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * 虚拟VTree的一个节点
 * @param view 设置了pageId或者elementId的节点，或者对应与设置了pageId的activity、dialog、fragment的根节点，只有在虚拟根节点时为null
 * @param parentNode 父节点,只有在虚拟根节点时为null
 * @param isVisible 表示这个节点是否可以被看见，不仅仅是visible的属性还有是否被父控件限制、遮挡之类的
 * @param visibleRect 表示真实的可见范围，主要用来和排除区域比较的
 * @param childrenList 子节点列表
 */

class VTreeNode(
        view: View?,
        var parentNode: VTreeNode? = null,
        var isVisible: Boolean = false,
        val visibleRect: Rect = Rect(),
        val childrenList: MutableList<VTreeNode> = mutableListOf(), var virtual: Boolean = false
): IVTreeNode {

    constructor(view: View?, isVisible: Boolean, virtual: Boolean) : this(
            view,
            null,
            isVisible = isVisible,
            virtual = virtual
    )

    override fun getParams(): Map<String, Any>? {
        return if (UIUtils.isMainThread()) {
            dataEntity?.customParams
        } else {
            var map: Map<String, Any>? = null
            val latch = CountDownLatch(1)
            EventDispatch.postMainRunnable(Runnable {
                dataEntity?.customParams?.let {
                    map = HashMap<String, Any>(it)
                }
                latch.countDown()
            })
            latch.await()
            map
        }
    }

    internal fun addParams(params: Map<String, Any>) {
        dataEntity?.customParams?.putAll(params)
    }

    /**
     * 对应的View
     */
    private var node: WeakReference<View?>? = null
    private var oid = ""
    private var isPage = false
    private var dataEntity: DataEntity? = null
    private val innerParams: HashMap<String, Any> = HashMap()
    val actualRect: Rect = Rect()
    var exposureArea: Int? = null
    private var exposureRate: Float? = null
    private var viewHashCode: String? = null //这里直接保存view的hashcode

    fun setExposureRate(rate: Float) {
        exposureRate = rate
    }

    override fun getExposureRate(): Float {
        return exposureRate
                ?: (calculateExposureRate().apply { exposureRate = this })
    }

    private fun calculateExposureRate(): Float {
        return (if (exposureArea != null) exposureArea!! else visibleRect.width() * visibleRect.height()) / (actualRect.width().toFloat() * actualRect.height())
    }

    fun setData(oid: String, isPage: Boolean, dataEntity: DataEntity?) {
        this.oid = oid
        this.isPage = isPage
        this.dataEntity = dataEntity
        this.viewHashCode = dataEntity?.viewHashCode
    }

    fun setInnerParams(params: Map<String, Any>?) {
        params ?: return
        innerParams.putAll(params)
    }

    override fun isPage(): Boolean {
        return isPage
    }

    init {
        initView(view)
    }

    private fun initView(view: View?) {
        this.node = WeakReference(view)
    }

    fun initNode(view: View?, isVisible: Boolean, isVirtual: Boolean): VTreeNode {
        initView(view)
        this.isVisible = isVisible
        this.virtual = isVirtual
        return this
    }

    override fun isVirtualNode(): Boolean {
        return virtual
    }

    override fun getInnerParam(key: String): Any? {
        return innerParams[key]
    }

    override fun getNode(): View? {
        return node?.get()
    }

    fun getDataEntity(): DataEntity? {
        return dataEntity
    }

    /**
     * 把数据全部更新到node上面，然后view对应的真正存储数据的地方删除掉
     */
    fun updateDynamicParamsAndDelete() {
        dataEntity?.dynamicParams?.get()?.viewDynamicParams?.apply {
            for (entry in entries) {
                dataEntity?.customParams?.put(entry.key ?: "", entry.value ?: "")
            }
        }
    }

    fun getViewDynamicParams(): WeakReference<IViewDynamicParamsProvider>? {
        return dataEntity?.dynamicParams
    }

    fun getEventParams(eventCode: String): IViewDynamicParamsProvider? {
        return dataEntity?.eventCallback?.get(eventCode)
    }

    /**
     * 获取在父控件的相对位置
     */
    override fun getPos(): Int? {
        return innerParams[InnerKey.VIEW_POSITION] as? Int?
    }

    private fun getReExposureFlag(): Int {
        return (innerParams[InnerKey.VIEW_RE_EXPOSURE_FLAG] as? Int?) ?: 0
    }

    override fun getOid(): String {
        return oid
    }

    override fun getIdentifier(): String? {
        return innerParams[InnerKey.VIEW_IDENTIFIER] as? String?
    }

    private fun getIdentifierSpm(): StringBuilder {
        val spmBuilder = StringBuilder()
        var currentNode = this
        while (currentNode.parentNode != null) {
            val oid = currentNode.getIdentifier() ?: viewHashCode
            spmBuilder.append(oid).append(":")
            currentNode = currentNode.parentNode!!
        }
        return spmBuilder
    }

    override fun getSpm(): String {
        val spmBuilder = StringBuilder()
        var currentNode = this
        while (currentNode.parentNode != null) {
            val oid = currentNode.getOid()
            val pos = currentNode.getPos()
            spmBuilder.append(oid)
            pos?.let {
                spmBuilder.append(":").append(pos)
            }
            spmBuilder.append("|")
            currentNode = currentNode.parentNode!!
        }
        if (spmBuilder.isEmpty()) {
            return ""
        }
        return spmBuilder.substring(0, spmBuilder.length - 1)
    }

    override fun getScm(): String {
        return getScmByEr().first
    }

    override fun getScmByEr(): Pair<String, Boolean> {
        val scmBuilder = StringBuilder()
        var currentNode = this
        var flag: Boolean = false
        while (currentNode.parentNode != null) {
            val result = DataReportInner.getInstance().configuration.referStrategy.buildScm(currentNode.getParams())
            flag = flag || result.second
            scmBuilder.append(result.first).append("|")
            currentNode = currentNode.parentNode!!
        }
        if (scmBuilder.isEmpty()) {
            return Pair("", flag)
        }
        return Pair(scmBuilder.substring(0, scmBuilder.length - 1), flag)
    }

    override fun equals(other: Any?): Boolean {
        if (other is VTreeNode) {
            return getOid() == other.getOid() && getReExposureFlag() == other.getReExposureFlag() &&
                    getIdentifierSpm().toString() == other.getIdentifierSpm().toString()
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(arrayOf(getOid(), getReExposureFlag(), getIdentifierSpm().toString()))
    }

    override fun getDebugHashCodeString(): String {
        return "${getOid()}:${getReExposureFlag()}:${getIdentifier()}:${viewHashCode}:${getIdentifierSpm()}"
    }

    override fun getUniqueCode(): Int {
        return Arrays.hashCode(arrayOf(getOid(), getReExposureFlag(), getIdentifier() ?: ""))
    }

    fun deepVirtualClone(): VTreeNode {
        val tempNode = ReusablePool.obtainVTreeNode(null, true, true)
        tempNode.setData(oid, isPage, dataEntity)
        tempNode.innerParams.putAll(innerParams)
        return tempNode
    }

    //================点击事件的时候保存的IContext===================
    var context: IContext? = null

    /**
     * 这个方法就是把一个节点完全拷贝到新的节点
     * parent和childrenlist除外
     */
    fun getDeepCopyNode(): VTreeNode {
        val newNode = ReusablePool.obtainVTreeNode(getNode(), isVisible, virtual)
        newNode.visibleRect.set(visibleRect)
        newNode.setData(oid, isPage, dataEntity)
        newNode.innerParams.putAll(innerParams)
        newNode.actualRect.set(actualRect)
        newNode.exposureArea = exposureArea
        context?.let {
            newNode.context = it
        }
        return newNode
    }

    private var mainThreadSpm: String? = null
    @MainThread
    override fun getSpmWithoutPos(): String {
        if (parentNode?.parentNode == null) {
            return oid
        }
        return mainThreadSpm ?: ("${oid}|${parentNode?.getSpmWithoutPos()}".apply { mainThreadSpm = this })
    }
}