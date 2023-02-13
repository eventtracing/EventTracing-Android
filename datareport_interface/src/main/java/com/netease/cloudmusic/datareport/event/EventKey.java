package com.netease.cloudmusic.datareport.event;

public class EventKey {

    /**
     * 页面曝光,
     * 1.有页面ID的页面曝光时发送。 2.在已经曝光的页面设置页面ID的时候发送
     */
    public static final String PAGE_VIEW = "_pv";

    /**
     * 页面结束曝光
     * 有页面ID的页面从曝光变为不曝光时发送
     */
    public static final String PAGE_DISAPPEAR = "_pd";

    /**
     * 元素曝光
     * 1.在有元素ID的页面元素的曝光状态由未曝光转到曝光时。 2.在已曝光元素设置元素ID时。
     */
    public static final String ELEMENT_VIEW = "_ev";

    /**
     * 元素反曝光
     * 1.在有元素ID的页面元素的曝光状态由曝光转到未曝光时。
     */
    public static final String ELEMENT_DISAPPEAR = "_ed";

    /**
     * 点击
     * 点击实现了相关 click Listener 的元素控件
     */
    public static final String VIEW_CLICK = "_ec";

    /**
     * 应用访问
     * 1.冷启动发送  2.默认APP进入后台30秒以后再次打开会发送
     */
    public static final String APP_VISIT = "_ac";

    /**
     * app进前台
     * 当App的第一个页面可见时上报
     */
    public static final String APP_IN = "_ai";

    /**
     * app退后台
     * 当App由可见状态变为不可见状态时上报
     */
    public static final String APP_OUT = "_ao";

    /**
     * 滑动事件
     */
    public static final String VIEW_SCROLL = "_es";
}
