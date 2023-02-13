package com.netease.cloudmusic.datareport.data;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.provider.IExposureCallback;
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider;
import com.netease.cloudmusic.datareport.report.InnerReportKeyKt;
import com.netease.cloudmusic.datareport.utils.BaseUtils;
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * DataEntity对应的操作类
 */
public class DataEntityOperator {

    /**
     * 获取元素id
     */
    static String getElementId(@Nullable DataEntity entity) {
        return entity == null ? null : entity.elementId;
    }

    /**
     * 设置元素id
     */
    static void setElementId(DataEntity entity, String elementId, boolean isChange) {
        if (entity != null) {
            if (isChange) {
                clearDataEntity(entity);
            }
            entity.elementId = elementId;
        }
    }

    /**
     * 设置动态自定义参数
     */
    static void setCustomParams(DataEntity entity, IViewDynamicParamsProvider provider) {
        if (entity == null || provider == null) {
            return;
        }
        entity.dynamicParams = new WeakReference<>(provider);
    }

    static void setExposureCallback(DataEntity entity, IExposureCallback callback) {
        if (entity == null || callback == null) {
            return;
        }
        entity.exposureCallback = new WeakReference<>(callback);
    }

    static void setEventCallback(DataEntity entity, String eventKey, IViewDynamicParamsProvider provider) {
        entity.eventCallback.put(eventKey, provider);
    }

    static IViewDynamicParamsProvider getViewDynamicParamsProvider(DataEntity entity) {
        if (entity == null || entity.dynamicParams == null) {
            return null;
        }
        return entity.dynamicParams.get();
    }

    static IExposureCallback getExposureCallback(DataEntity entity) {
        if (entity == null || entity.exposureCallback == null) {
            return null;
        }
        return entity.exposureCallback.get();
    }

    /**
     * 获取页面id
     */
    static String getPageId(@Nullable DataEntity entity) {
        return entity == null ? null : entity.pageId;
    }

    /**
     * 设置页面id
     */
    static void setPageId(DataEntity entity, String pageId, boolean isChange) {
        if (entity != null) {
            if (isChange) {
                clearDataEntity(entity);
            }
            entity.pageId = pageId;
        }
    }

    static void setHashCode(DataEntity entity, int hashCode) {
        if (entity != null) {
            entity.viewHashCode = Integer.toString(hashCode);
        }
    }

    /**
     * 设置自定义参数
     */
    static void setCustomParams(DataEntity entity, Map<String, ?> customParams) {
        if (entity == null || customParams == null) {
            return;
        }
        entity.customParams.putAll(customParams);
    }

    /**
     * 设置自定义参数
     */
    static void setCustomParams(DataEntity entity, String key, Object value) {
        if (entity == null || TextUtils.isEmpty(key)) {
            return;
        }
        entity.customParams.put(key, value);
    }

    /**
     * 删除自定义参数
     */
    static void removeCustomParam(DataEntity entity, String key) {
        if (entity != null) {
            entity.customParams.remove(key);
        }
    }

    /**
     * 清空自定义参数
     */
    static void removeAllCustomParams(DataEntity entity) {
        if (entity != null) {
            entity.customParams.clear();
            entity.dynamicParams = null;
        }
    }

    private static void clearDataEntity(DataEntity entity) {
        entity.pageId = null;
        entity.elementId = null;
        entity.viewHashCode = null;
        entity.innerParams.clear();
        entity.customParams.clear();
        entity.eventCallback.clear();
        entity.dynamicParams = null;
        entity.exposureCallback = null;
    }

    /**
     * 保存上报对象的内部参数
     */
    static void putInnerParam(DataEntity entity, String key, Object value) {
        if (entity == null || TextUtils.isEmpty(key)) {
            return;
        }
        entity.innerParams.put(key, value);
    }

    /**
     * 获取上报对象的内部参数
     */
    @Nullable
    static Object getInnerParam(DataEntity entity, String key) {
        if (entity == null) {
            return null;
        }
        return entity.innerParams.get(key);
    }

    /**
     * 删除上报对象的内部参数
     */
    static void removeInnerParam(DataEntity entity, String key) {
        if (entity != null) {
            entity.innerParams.remove(key);
        }
    }

}
