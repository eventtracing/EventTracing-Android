package com.netease.cloudmusic.datareport.eventtracing

import android.view.View
import com.netease.cloudmusic.datareport.DataReportUtils
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.policy.ReportPolicy
import com.netease.cloudmusic.datareport.policy.VirtualParentConfig
import com.netease.cloudmusic.datareport.provider.IExposureCallback
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider

/**
 * 曙光埋点的业务层封装
 */
class NodeBuilder private constructor(viewNode: Any?) {

    private val node: Any? = viewNode

    companion object {

        private fun getIdentifyHashCode(data: Any): String {
            return if (data is IDataIdentifyProvider) {
                data.getIdentify()
            } else {
                return data.hashCode().toString()
            }
        }

        fun getNodeBuilder(viewNode: Any?): NodeBuilder {
            return NodeBuilder(viewNode)
        }

        fun getSubMenuId(menuId: Int): String {
            return DataReportUtils.getSubMenuId(menuId)
        }

        fun getNodeBuilderForSubMenu(id: Int): NodeBuilder {
            return NodeBuilder(DataReportUtils.getSubMenuId(id))
        }

        fun setPageId(node: Any?, pageId: String) {
            node ?: return
            DataReport.getInstance().setPageId(node, pageId)
        }

        /**
         * 设置视图的element id
         */
        fun setElementId(node: Any?, elementId: String) {
            node ?: return
            DataReport.getInstance().setElementId(node, elementId)
        }

        /**
         * 清除id，如果设置了pageId就会清除pageId，如果设置了elementId就会清除elementId
         */
        fun clearOid(node: Any?) {
            node ?: return
            DataReport.getInstance().clearOid(node)
        }

        /**
         * 设置单个视图的上报策略。不设置就改用全局默认的
         */
        fun setReportPolicy(node: Any?, policy: ReportPolicy) {
            node ?: return
            DataReport.getInstance().setReportPolicy(node, policy)
        }

        /**
         * 设置节点的逻辑父亲
         */
        fun setLogicParent(node: Any?, logicParent: Any) {
            node ?: return
            DataReport.getInstance().setLogicParent(node, logicParent)
        }

        /**
         * 清楚节点的逻辑父亲
         */
        fun deleteLogicParent(node: Any) {
            node ?: return
            DataReport.getInstance().deleteLogicParent(node)
        }

        /**
         * 设置元素是否需要上报曝光结束， 默认不会上报
         */
        fun setElementExposureEnd(node: Any?, exposure: Boolean){
            node ?: return
            DataReport.getInstance().setElementExposureEnd(node, exposure)
        }

        /**
         * 允许外部设置元素的标志，用以去重
         */
        fun setReuseIdentifier(node: Any?, data: Any) {
            node ?: return
            val identifier = getIdentifyHashCode(data)
            DataReport.getInstance().setReuseIdentifier(node, identifier)
        }

        /**
         * 允许外部设置元素的标志，用以去重
         */
        fun setReuseIdentifierHashCode(node: Any?, identifier: String) {
            node ?: return
            DataReport.getInstance().setReuseIdentifier(node, identifier)
        }

        /**
         * 设置一个View的可见性的排除区域margin
         */
        fun setVisibleMargin(node: Any?, left: Int, top: Int, right: Int, bottom: Int) {
            node ?: return
            DataReport.getInstance().setVisibleMargin(node, left, top, right, bottom)
        }

        /**
         * 需要跳转的oid
         */
        fun setToOid(node: Any?, vararg oid: String) {
            node ?: return
            DataReport.getInstance().setToOid(node, * oid)
        }

        /**
         * @param isAlertView 是否设置成alert view
         * @param priority 设置显示的优先级，大的会把小的遮挡
         * 设置类似于悬浮窗的挂靠，就是把View挂靠在最右边的根节点，并且要在悬浮窗之前
         */
        fun setViewAsAlert(node: Any?, isAlertView: Boolean, priority: Int) {
            node ?: return
            DataReport.getInstance().setViewAsAlert(node, isAlertView, priority)
        }


        /**
         * 把一个普通的page节点，强制定义成rootpage
         * 主要是给RN使用
         * @param flag true表示成为rootpage，false表示取消成为rootpage
         */
        fun setViewAsRootPage(node: Any?, flag: Boolean) {
            node?.let {
                DataReport.getInstance().setViewAsRootPage(it, flag)
            }
        }

        /**
         * 给这个节点设置一个虚拟的父节点，而他原来的父节点会变成虚拟父节点的父节点
         * 这个方法的作用主要是为了方便统一VTree的树状结构
         * 虚拟父节点必须是一个元素，虚拟父节点必须被指定identifier
         * @param elementId 元素id
         * @param identifier 用于唯一定位这个节点
         * @param params 传入的参数
         */
        fun setVirtualParentNode(node: Any?, elementId: String, identifier: String, config: VirtualParentConfig) {
            node ?: return
            DataReport.getInstance().setVirtualParentNode(node, elementId, identifier, config)
        }

        /**
         * 设置业务逻辑可见性，如果设置了不可见，和本身真实的不可见相同对待
         * @param isVisible
         */
        fun setLogicVisible(node: Any?, isVisible: Boolean) {
            node?.let {
                DataReport.getInstance().setLogicVisible(it, isVisible)
            }
        }

        /**
         * 设置曝光的最小时间，这个设置只针对element生效，所有的page都是立即曝光的
         * 这个设置优先级高于全局的曝光最小时间
         * @param time 单位毫秒
         */
        fun setExposureMinTime(node: Any?, time: Long) {
            node?.let {
                DataReport.getInstance().setExposureMinTime(it, time)
            }
        }

        /**
         * 设置曝光的最小比例，露出指定比例的范围，才做曝光
         * 该逻辑同样适用于“可见回调”
         * 不处理多重遮挡场景，那个太复杂了，没有必要
         * 注意: 单次遮挡的时候，如果一个节点被判定不可见，其子节点直接当做不可见
         * @param rate 比例，比如 0.1f, 可见面积 / 总面积 如果小于 0.1f 表示不可见
         */
        fun setExposureMinRate(node: Any?, rate: Float) {
            node?.let {
                DataReport.getInstance().setExposureMinRate(it, rate)
            }
        }

        /**
         * 设置曝光与反曝光的回调接口，在一个view即将做曝光或者反曝光的时候就会调用
         * 注意这里的callback会被view强引用，只要view不销毁callback也不会销毁。这里要注意内存泄漏
         * @param callback
         */
        fun setExposureCallback(node: Any?, callback: IExposureCallback) {
            node?.let {
                DataReport.getInstance().setExposureCallback(it, callback)
            }
        }

        /**
         * 设置滚动上报
         */
        fun setScrollEventEnable(view: View?, enable: Boolean) {
            view ?: return
            DataReport.getInstance().setScrollEventEnable(view, enable)
        }

        fun addEventParamsCallback(node: Any?, eventIds: Array<String>, provider: IViewDynamicParamsProvider) {
            node?.let {
                DataReport.getInstance().addEventParamsCallback(it, eventIds, provider)
            }
        }

        fun setClickParamsCallback(node: Any?, provider: IViewDynamicParamsProvider) {
            node?.let {
                DataReport.getInstance().setClickParamsCallback(it, provider)
            }
        }

        /**
         * 设置自定义参数
         */
        fun params(node: Any?): ParamBuilder {
            return params(node, true)
        }

        /**
         * 设置自定义参数
         */
        fun params(node: Any?, isClear: Boolean): ParamBuilder {
            return ParamBuilder(node).apply {
                if (isClear) {
                    clear()
                }
            }
        }
    }

    /**
     * 设置视图的page id
     */
    fun setPageId(pageId: String): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setPageId(node, pageId)
        return this
    }

    /**
     * 设置视图的element id
     */
    fun setElementId(elementId: String): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setElementId(node, elementId)
        return this
    }

    /**
     * 清除id，如果设置了pageId就会清除pageId，如果设置了elementId就会清除elementId
     */
    fun clearOid(): NodeBuilder {
        node ?: return this
        DataReport.getInstance().clearOid(node)
        return this
    }

    /**
     * 设置单个视图的上报策略。不设置就改用全局默认的
     */
    fun setReportPolicy(policy: ReportPolicy): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setReportPolicy(node, policy)
        return this
    }

    /**
     * 设置节点的逻辑父亲
     */
    fun setLogicParent(logicParent: Any): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setLogicParent(node, logicParent)
        return this
    }

    /**
     * 清楚节点的逻辑父亲
     */
    fun deleteLogicParent(): NodeBuilder {
        node ?: return this
        DataReport.getInstance().deleteLogicParent(node)
        return this
    }

    /**
     * 允许外部设置元素的标志，用以去重
     */
    fun setReuseIdentifier(data: Any): NodeBuilder {
        node ?: return this
        val identifier = getIdentifyHashCode(data)
        DataReport.getInstance().setReuseIdentifier(node, identifier)
        return this
    }

    /**
     * 允许外部设置元素的标志，用以去重
     */
    fun setReuseIdentifierHashCode(identifier: String): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setReuseIdentifier(node, identifier)
        return this
    }

    /**
     * 设置一个View的可见性的排除区域margin
     */
    fun setVisibleMargin(left: Int, top: Int, right: Int, bottom: Int): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setVisibleMargin(node, left, top, right, bottom)
        return this
    }

    /**
     * 需要跳转的oid
     */
    fun setToOid(vararg oid: String): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setToOid(node, * oid)
        return this
    }

    /**
     * @param isAlertView 是否设置成alert view
     * @param priority 设置显示的优先级，大的会把小的遮挡
     * 设置类似于悬浮窗的挂靠，就是把View挂靠在最右边的根节点，并且要在悬浮窗之前
     */
    fun setViewAsAlert(isAlertView: Boolean, priority: Int = 1): NodeBuilder {
        node ?: return this
        DataReport.getInstance().setViewAsAlert(node, isAlertView, priority)
        return this
    }

    /**
     * 把一个普通的page节点，强制定义成rootpage
     * 主要是给RN使用
     * @param flag true表示成为rootpage，false表示取消成为rootpage
     */
    fun setViewAsRootPage(flag: Boolean): NodeBuilder {
        node ?: return this
        node.let {
            DataReport.getInstance().setViewAsRootPage(it, flag)
        }
        return this
    }

    /**
     * 给这个节点设置一个虚拟的父节点，而他原来的父节点会变成虚拟父节点的父节点
     * 这个方法的作用主要是为了方便统一VTree的树状结构
     * 虚拟父节点必须是一个元素，虚拟父节点必须被指定identifier
     * @param elementId 元素id(element)
     * @param data 用于唯一定位这个节点
     * @param config 传入的参数
     */
    fun setVirtualParentNode(elementId: String, data: Any, config: VirtualParentConfig): NodeBuilder {
        node ?: return this
        val identifier = getIdentifyHashCode(data)
        DataReport.getInstance().setVirtualParentNode(node, elementId, identifier, config)
        return this
    }

    /**
     * 设置业务逻辑可见性，如果设置了不可见，和本身真实的不可见相同对待
     * @param isVisible
     */
    fun setLogicVisible(isVisible: Boolean): NodeBuilder {
        node?.let {
            DataReport.getInstance().setLogicVisible(it, isVisible)
        }
        return this
    }

    /**
     *
     */
    fun setElementExposureEnd(exposure: Boolean): NodeBuilder{
        node?.let {
            DataReport.getInstance().setElementExposureEnd(it, exposure)
        }
        return this
    }

    /**
     * 设置曝光的最小时间，这个设置只针对element生效，所有的page都是立即曝光的
     * 这个设置优先级高于全局的曝光最小时间
     * @param time 单位毫秒
     */
    fun setExposureMinTime(time: Long): NodeBuilder {
        node?.let {
            DataReport.getInstance().setExposureMinTime(it, time)
        }
        return this
    }

    /**
     * 设置曝光的最小比例，露出指定比例的范围，才做曝光
     * 该逻辑同样适用于“可见回调”
     * 不处理多重遮挡场景，那个太复杂了，没有必要
     * 注意: 单次遮挡的时候，如果一个节点被判定不可见，其子节点直接当做不可见
     * @param rate 比例，比如 0.1f, 可见面积 / 总面积 如果小于 0.1f 表示不可见
     */
    fun setExposureMinRate(rate: Float): NodeBuilder {
        node?.let {
            DataReport.getInstance().setExposureMinRate(it, rate)
        }
        return this
    }

    /**
     * 设置曝光与反曝光的回调接口，在一个view即将做曝光或者反曝光的时候就会调用
     * 注意这里的callback会被view强引用，只要view不销毁callback也不会销毁。这里要注意内存泄漏
     * @param callback
     */
    fun setExposureCallback(callback: IExposureCallback): NodeBuilder {
        node?.let {
            DataReport.getInstance().setExposureCallback(it, callback)
        }
        return this
    }

    /**
     * 设置滚动上报
     */
    fun setScrollEventEnable(enable: Boolean): NodeBuilder {
        (node as? View)?.let {
            DataReport.getInstance().setScrollEventEnable(it, enable)
        }
        return this
    }

    fun addEventParamsCallback(eventIds: Array<String>, provider: IViewDynamicParamsProvider): NodeBuilder {
        node?.let {
            DataReport.getInstance().addEventParamsCallback(it, eventIds, provider)
        }
        return this
    }

    fun setClickParamsCallback(provider: IViewDynamicParamsProvider): NodeBuilder {
        node?.let {
            DataReport.getInstance().setClickParamsCallback(it, provider)
        }
        return this
    }

    /**
     * 设置自定义参数
     */
    fun params(isClear: Boolean): ParamBuilder {
        return ParamBuilder(node).apply {
            if (isClear) {
                clear()
            }
        }
    }
    /**
     * 设置自定义参数
     */
    fun params(): ParamBuilder {
        return params(true)
    }


}