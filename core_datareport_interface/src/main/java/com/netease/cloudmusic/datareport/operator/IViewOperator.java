package com.netease.cloudmusic.datareport.operator;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.policy.ReferConsumeOption;
import com.netease.cloudmusic.datareport.policy.ReportPolicy;
import com.netease.cloudmusic.datareport.policy.TransferType;
import com.netease.cloudmusic.datareport.policy.VirtualParentConfig;
import com.netease.cloudmusic.datareport.provider.IExposureCallback;
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider;

import java.util.Map;

/**
 * View操作的api
 */
public interface IViewOperator {

    /**
     * 自定义级别的上报
     *
     * @param object     Activity/Dialog/View
     * @param map 数据
     */
    IViewOperator setCustomParams(Object object, Map<String, ?> map);

    /**
     * 自定义页面级别的上报
     *
     * @param object Activity/Dialog/View
     * @param key    key
     * @param value  value
     */
    IViewOperator setCustomParams(Object object, String key, Object value);

    /**
     * 设置动态参数
     * 注意：内部使用弱引用存储，业务方必须要用强引用保存，否则内存会被回收
     *
     * @param object   Activity/Dialog/View
     * @param provider 参数
     */
    IViewOperator setDynamicParams(Object object, IViewDynamicParamsProvider provider);

    /**
     * 设置页面所对应的pageId
     *
     * @param object Activity/Dialog/View
     * @param pageId pageId
     */
    IViewOperator setPageId(Object object, String pageId);


    /**
     * 设置元素所对应的elementId
     *
     * @param object    Dialog/View
     * @param elementId elementId
     */
    IViewOperator setElementId(Object object, String elementId);

    /**
     * 清除id，如果设置了pageId就会清除pageId，如果设置了elementId就会清除elementId
     */
    IViewOperator clearOid(Object object);

    /**
     * 设置View的位置信息
     */
    IViewOperator setPosition(Object object, int pos);

    /**
     * 移除页面所对应的参数
     *
     * @param object Activity/Dialog/View
     * @param key    参数的key
     */
    IViewOperator removeCustomParam(Object object, String key);

    /**
     * 清空页面所对应的参数
     *
     * @param object Activity/Dialog/View
     */
    IViewOperator resetCustomParams(Object object);


    /**
     * 设置单个视图的上报策略。不设置就改用全局默认的
     *
     * @param object Activity/Dialog/View
     * @param policy 上报策略
     */
    IViewOperator setReportPolicy(Object object, ReportPolicy policy);

    /**
     * 把当前的View对应的数据和已经构建的树的节点的数据隔离开来
     * 那么接下来你设置的新的数据，不会同步到已经存在的虚拟树的节点上面
     * 注意，调用了deepCloneData方法之后，生成的虚拟树还是会同步数据
     * @param object
     * @return
     */
    IViewOperator deepCloneData(Object object);

    /**
     * 设置页面的逻辑父亲
     */
    IViewOperator setLogicParent(Object child, Object logicParent);

    IViewOperator deleteLogicParent(Object object);

    /**
     * 允许外部设置元素的标志，用以去重
     *
     * @param object    视图对象
     * @param identifier 唯一标志
     */
    IViewOperator setReuseIdentifier(Object object, String identifier);

    /**
     * 设置一个View的可见性的排除区域margin
     */
    IViewOperator setVisibleMargin(Object object, int left, int top, int right, int bottom);

    /**
     * 需要跳转的oid
     * @param object 点击跳转的View
     * @param oid 跳转的oid
     */
    IViewOperator setToOid(Object object, String... oid);

    /**
     * 对控件进行重新曝光，该控件的所有子控件都会重新曝光
     * 主要就是先标记这个控件，然后重新生成VTree，然后进行反曝光和曝光操作
     * @param views
     */
    IViewOperator reExposureView(Object... views);

    /**
     * @param object 要设置的目标view
     * @param isAlertView 是否设置成alert view
     * @param priority 设置显示的优先级，大的会把小的遮挡
     * 设置类似于悬浮窗的挂靠，就是把View挂靠在最右边的根节点，并且要在悬浮窗之前
     */
    IViewOperator setViewAsAlert(Object object, boolean isAlertView, int priority);

    /**
     * 重新生成VTree，然后会根据新生成的树进行反曝光和曝光操作
     * 这个函数主要用在AOP没有覆盖到的View的可见性变化
     * @param object 变化的View
     */
    IViewOperator reBuildVTree(@NonNull Object object);

    /**
     * 把一个普通的page节点，强制定义成rootpage
     * 主要是给RN使用
     * @param view 需要被定义成rootpage的view
     * @param flag true表示成为rootpage，false表示取消成为rootpage
     */
    IViewOperator setViewAsRootPage(@NonNull Object view, boolean flag);

    /**
     * 给这个节点设置一个虚拟的父节点(element)，而他原来的父节点会变成虚拟父节点的父节点
     * 这个方法的作用主要是为了方便统一VTree的树状结构
     * 虚拟父节点必须是一个元素，虚拟父节点必须被指定identifier
     * @param elementId 元素id
     * @param identifier 用于唯一定位这个节点
     * @param config 传入的参数
     */
    IViewOperator setVirtualParentNode(@NonNull Object view, String elementId, String identifier, @Nullable VirtualParentConfig config);

    /**
     * 给这个节点设置一个虚拟的父节点(page)，而他原来的父节点会变成虚拟父节点的父节点
     * 这个方法的作用主要是为了方便统一VTree的树状结构
     * 虚拟父节点必须是一页面，虚拟父节点必须被指定identifier
     * @param pageId 页面id
     * @param identifier 用于唯一定位这个节点
     * @param config 传入的参数
     */
    IViewOperator setVirtualParentPageNode(@NonNull Object view, String pageId, String identifier, @Nullable VirtualParentConfig config);


    /**
     * 清除这个节点的虚拟父节点
     * @param view
     * @return
     */
    IViewOperator clearVirtualParentNode(@NonNull Object view);


    /**
     * 设置业务逻辑可见性，如果设置了不可见，和本身真实的不可见相同对待
     * @param view
     * @param isVisible
     * @return
     */
    IViewOperator setLogicVisible(@NonNull Object view, boolean isVisible);

    /**
     * 设置曝光的最小时间，这个设置只针对element生效，所有的page都是立即曝光的
     * 这个设置优先级高于全局的曝光最小时间
     * @param view
     * @param time 单位毫秒
     * @return
     */
    @Deprecated
    IViewOperator setExposureMinTime(@NonNull Object view, long time);

    /**
     * 设置曝光的最小比例，露出指定比例的范围，才做曝光
     * 该逻辑同样适用于“可见回调”
     * 不处理多重遮挡场景，那个太复杂了，没有必要
     * 注意: 单次遮挡的时候，如果一个节点被判定不可见，其子节点直接当做不可见
     * @param view
     * @param rate 比例，比如 0.1f, 可见面积 / 总面积 如果小于 0.1f 表示不可见
     * @return
     */
    @Deprecated
    IViewOperator setExposureMinRate(@NonNull Object view, float rate);

    /**
     * 设置曝光与反曝光的回调接口，在一个view即将做曝光或者反曝光的时候就会调用
     * 注意这里的callback会被view强引用，只要view不销毁callback也不会销毁。这里要注意内存泄漏
     * @param view
     * @param callback
     * @return
     */
    IViewOperator setExposureCallback(@NonNull Object view, IExposureCallback callback);

    /**
     * 设置元素是否需要上报曝光结束， 默认不会上报
     * @param view
     * @param enable
     * @return
     */
    IViewOperator setElementExposureEnd(Object view, boolean enable);

    /**
     * 给对应的事件参数设置对象级别的数据，通过动态钩子的形式添加
     * @param eventIds 对应事件的id的列表
     * @param provider 回调的钩子
     */
    IViewOperator addEventParamsCallback(Object view, String[] eventIds, IViewDynamicParamsProvider provider);

    /**
     * 给点击事件参数设置对象级别的数据，通过动态钩子的形式添加
     * @param provider 回调的钩子
     */
    IViewOperator setClickParamsCallback(Object view, IViewDynamicParamsProvider provider);

    /**
     * 给发生事件的view转移事件
     * @param view 当前发生事件的view
     * @param type 类型，主要有这么集中：EventTransferPolicy.TYPE_TARGET_VIEW, EventTransferPolicy.TYPE_FIND_UP_OID, EventTransferPolicy.TYPE_FIND_DOWN_OID
     * @param targetView 需要转移到的目标view， 可以为null
     * @param targetOid 需要转移到的目标oid， 可以为null
     * @return
     */
    IViewOperator setEventTransferPolicy(Object view, @TransferType int type, @Nullable View targetView, @Nullable String targetOid);

    /**
     * 是否给对应的view设置滑动上报事件
     * @param view 滑动的view
     * @param enable 是否支持滑动事件上报
     * @return
     */
    IViewOperator setScrollEventEnable(View view, boolean enable);

    /**
     * 设置是否开启activity的layout的变化的监听
     * @param view
     * @param enable
     * @return
     */
    IViewOperator setEnableLayoutObserver(Object view, boolean enable);

    /**
     * 设置是否是透明主题的activity
     * @param activity
     * @param isTransparent
     * @return
     */
    IViewOperator setTransparentActivity(Activity activity, boolean isTransparent);

    /**
     * 设置透明activity的时候，需要忽略掉没有用的activity
     * @param activity
     * @param isIgnore
     * @return
     */
    IViewOperator setIgnoreActivity(Activity activity, boolean isIgnore);

    /**
     * 设置节点的所有事件忽略refer
     * @param view
     * @param enable
     * @return
     */
    IViewOperator setNodeIgnoreRefer(Object view, boolean enable);

    /**
     * 在获取最叶子page 的时候，设置是否忽略该节点
     */
    IViewOperator setIgnoreChildPage(Object view);

    /**
     * 设置一个节点的refer是否失效，如果失效那么这个节点所产生的所有归因都不起作用
     * @param view
     * @return
     */
    IViewOperator setReferMute(Object view, boolean enable);

    /**
     * 设置子页面的曝光是否产生Refer
     * @param view
     * @param enable
     * @return
     */
    IViewOperator setSubPageGenerateReferEnable(Object view, boolean enable);

    /**
     * 设置子页面的曝光消费refer
     * @param view
     * @param option refer类型的枚举
     * @return
     */
    IViewOperator setSubPageConsumeReferOption(Object view, ReferConsumeOption option);

}
