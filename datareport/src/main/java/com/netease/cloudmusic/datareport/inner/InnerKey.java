package com.netease.cloudmusic.datareport.inner;

public class InnerKey {

    /**
     * 上报的策略，曝光与点击是否上报
     */
    public static final String VIEW_REPORT_POLICY = "view_report_policy";

    /**
     * 逻辑挂靠的父节点
     */
    public static final String LOGIC_PARENT = "logic_parent";

    /**
     * 逻辑挂靠的子节点
     */
    public static final String LOGIC_CHILDREN = "logic_children";

    /**
     * 确定一个view的唯一标示
     */
    public static final String VIEW_IDENTIFIER = "view_identifier";

    /**
     * 设置一个view可见的margin
     */
    public static final String VIEW_VISIBLE_MARGIN = "view_visible_margin";

    /**
     * view的position
     */
    public static final String VIEW_POSITION = "view_position";

    /**
     * 需要跳转的oid的设置
     */
    public static final String VIEW_TO_OID = "view_to_oid";

    /**
     * 用来重新曝光的标识
     */
    public static final String VIEW_RE_EXPOSURE_FLAG = "view_re_exposure_flag";

    /**
     * 设置类似于悬浮窗的挂靠，就是把View挂靠在最右边的根节点，并且要在悬浮窗之前
     */
    public static final String VIEW_ALERT_FLAG = "view_alert_flag";

    /**
     * 设置类似于悬浮窗的挂靠，就是把View挂靠在最右边的根节点，并且要在悬浮窗之前, 的优先级，优先级大的会遮挡优先级小的
     */
    public static final String VIEW_ALERT_PRIORITY = "view_alert_priority";

    /**
     * 把一个普通的page节点，强制定义成rootpage
     */
    public static final String VIEW_AS_ROOT_PAGE = "view_as_root_page";

    /**
     * 给这个节点设置一个虚拟的父节点，而他原来的父节点会变成虚拟父节点的父节点
     */
    public static final String VIEW_VIRTUAL_PARENT_NODE = "view_virtual_parent_node";

    /**
     * 设置一个view是逻辑不可见的，和正常不可见相同的处理逻辑
     */
    public static final String VIEW_LOGIC_VISIBLE = "view_logic_visible";

    /**
     * 最小曝光比例
     */
    public static final String VIEW_EXPOSURE_MIN_RATE = "view_exposure_min_rate";

    /**
     * 最小曝光时长
     */
    public static final String VIEW_EXPOSURE_MIN_TIME = "view_exposure_min_time";

    /**
     * 元素是否上报曝光结束
     */
    public static final String VIEW_ELEMENT_EXPOSURE_END = "view_element_exposure_end";

    /**
     * 给发生事件的view转移事件
     */
    public static final String VIEW_EVENT_TRANSFER = "view_event_transfer";

    /**
     * 设置activity是否监听layout的变化
     */
    public static final String VIEW_ENABLE_LAYOUT_OBSERVER = "view_enable_layout_observer";

    /**
     * 设置是一个透明主题的activity
     */
    public static final String VIEW_TRANSPARENT_ACTIVITY = "view_transparent_activity";


    /**
     * 多个activity合并的时候设置这个activity忽略掉
     */
    public static final String VIEW_IGNORE_ACTIVITY = "view_ignore_activity";

    /**
     * 设置节点的所有事件都忽略refer
     */
    public static final String VIEW_IGNORE_REFER = "view_ignore_refer";

    /**
     * 在获取最叶子page 的时候，设置是否忽略该节点
     */
    public static final String VIEW_IGNORE_CHILD_PAGE = "view_ignore_child_page";

    /**
     * 设置该标签会忽略所有的refer
     * 与VIEW_IGNORE_REFER的区别，VIEW_IGNORE_REFER设置完之后对他的上下级也会生效，VIEW_REFER_MUTE只对自己生效
     */
    public static final String VIEW_REFER_MUTE = "view_refer_mute";
}