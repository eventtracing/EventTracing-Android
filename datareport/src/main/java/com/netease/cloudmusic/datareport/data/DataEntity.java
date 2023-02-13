package com.netease.cloudmusic.datareport.data;

import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.provider.IExposureCallback;
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储某个对象所有上报数据的实体类
 * 成员均只有包访问权限 数据存取需要通过操作类进行
 */
public class DataEntity {
    /**
     * 元素id
     */
    public String elementId;

    /**
     * 自定义参数
     */
    public ConcurrentHashMap<String, Object> customParams = new ConcurrentHashMap<>(1);

    /**
     * 页面id
     */
    public String pageId;

    public String viewHashCode;

    /**
     * 上报对象内部参数
     */
    public ConcurrentHashMap<String, Object> innerParams = new ConcurrentHashMap<>(1);

    /**
     * 元素自定义动态参数，在最终上报时sdk从业务侧取，保证取值的有效。
     * eg：关注按钮点击之后需要上报关注态。
     */
    @Nullable
    public WeakReference<IViewDynamicParamsProvider> dynamicParams;

    /**
     * 当view发生曝光或者反曝光的时候会调用这个回调
     */
    @Nullable
    public WeakReference<IExposureCallback> exposureCallback;

    public WeakHashMap<String, IViewDynamicParamsProvider> eventCallback = new WeakHashMap<>();

    public DataEntity deepClone() {
        DataEntity cloneEntity = new DataEntity();
        cloneEntity.elementId = elementId;
        cloneEntity.pageId = pageId;
        cloneEntity.viewHashCode = viewHashCode;
        cloneEntity.dynamicParams = dynamicParams;
        cloneEntity.exposureCallback = exposureCallback;
        cloneEntity.eventCallback = eventCallback;
        cloneEntity.customParams.putAll(customParams);
        cloneEntity.innerParams.putAll(innerParams);
        return cloneEntity;
    }

    @Override
    public String toString() {
        return "DataEntity{" +
                "elementId='" + elementId + '\'' +
                ", customParams=" + customParams +
                ", pageId='" + pageId + '\'' +
                ", innerParams=" + innerParams +
                '}';
    }

}
