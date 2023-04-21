package com.netease.cloudmusic.datareport.data;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.R;
import com.netease.cloudmusic.datareport.policy.MenuNode;
import com.netease.cloudmusic.datareport.provider.IExposureCallback;
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider;
import com.netease.cloudmusic.datareport.inner.InnerKey;
import com.netease.cloudmusic.datareport.vtree.VTreeManager;
import com.netease.cloudmusic.datareport.vtree.VTreeUtilsKt;
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap;
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode;
import com.netease.cloudmusic.datareport.vtree.logic.LogicMenuManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_TRANSPARENT_ACTIVITY;

/**
 * 数据读写的代理对象实例，通过该类进行上报数据的设置
 */
public class DataRWProxy {

    /**
     * 设置元素id
     */
    public static void setElementId(Object object, String elementId) {
        DataEntity dataEntity = getDataEntity(object, true);

        boolean isChange = false;
        if (dataEntity.pageId != null || (dataEntity.elementId != null && !dataEntity.elementId.equals(elementId))) {
            String oldId = dataEntity.pageId;
            if (oldId == null) {
                oldId = dataEntity.elementId;
            }
            dataEntity = copyDataEntity(object, dataEntity);
            isChange = true;
            //TODO 暂时不要弹窗
//            if (DataReportInner.getInstance().isDebugMode() && elementId != null && DataEntityOperator.getInnerParam(dataEntity, InnerKey.VIEW_IDENTIFIER) == null) {
//                new AlertDialog.Builder(VTreeUtilsKt.getView(object).getContext()).setTitle("曙光埋点错误警告").setMessage("埋点覆盖设置"+oldId+"被"+elementId+"覆盖！！！").show();
//            }
        }

        DataEntityOperator.setElementId(dataEntity, elementId, isChange);
        DataEntityOperator.setHashCode(dataEntity, object.hashCode());
    }

    private static DataEntity copyDataEntity(Object object, DataEntity dataEntity) {
        View view = VTreeUtilsKt.getView(object);
        if (view != null) {
            VTreeMap vTreeMap = VTreeManager.INSTANCE.getCurrentVTreeInfo();
            if (vTreeMap != null) {
                VTreeNode node = vTreeMap.getTreeMap().get(view);
                if (node != null) {
                    if (node.getDataEntity() == dataEntity) {
                        dataEntity = dataEntity.deepClone();
                        DataBinder.setDataEntity(view, dataEntity);
                    }
                }
            }
        }
        return dataEntity;
    }

    /**
     * 获取元素id
     */
    public static String getElementId(Object object) {
        return DataEntityOperator.getElementId(getDataEntity(object, false));
    }

    /**
     * 设置动态参数
     */
    public static void setViewDynamicParam(Object object, IViewDynamicParamsProvider provider){
        View view = VTreeUtilsKt.getView(object);
        if (view != null) {
            view.setTag(R.id.key_data_dynamic, provider);
            DataEntityOperator.setCustomParams(getDataEntity(view, true), provider);
        } else if (object instanceof MenuNode) {
            LogicMenuManager.INSTANCE.addDynamicParams(((MenuNode) object).getActivity(), provider);
            DataEntityOperator.setCustomParams(getDataEntity(object, true), provider);
        }
    }

    public static IViewDynamicParamsProvider getViewDynamicParam(Object object) {
        return DataEntityOperator.getViewDynamicParamsProvider(getDataEntity(object, false));
    }

    public static void setExposureCallback(Object object, IExposureCallback callback) {
        View view = VTreeUtilsKt.getView(object);
        if (view != null) {
            view.setTag(R.id.key_data_callback, callback);
            DataEntityOperator.setExposureCallback(getDataEntity(view, true), callback);
        }
    }

    public static IExposureCallback getExposureCallback(Object object) {
        return DataEntityOperator.getExposureCallback(getDataEntity(object, false));
    }

    /**
     * 设置页面id
     */
    public static void setPageId(Object object, String pageId) {
        DataEntity dataEntity = getDataEntity(object, true);

        boolean isChange = false;
        if (dataEntity.elementId != null || (dataEntity.pageId != null && !dataEntity.pageId.equals(pageId))) {
            String oldId = dataEntity.pageId;
            if (oldId == null) {
                oldId = dataEntity.elementId;
            }
            dataEntity = copyDataEntity(object, dataEntity);
            isChange = true;
            //TODO 暂时不要弹窗
//            if (DataReportInner.getInstance().isDebugMode() && pageId != null && DataEntityOperator.getInnerParam(dataEntity, InnerKey.VIEW_IDENTIFIER) == null) {
//                new AlertDialog.Builder(VTreeUtilsKt.getView(object).getContext()).setTitle("曙光埋点错误警告").setMessage("埋点覆盖设置"+oldId+"被"+pageId+"覆盖！！！").show();
//            }
        }

        DataEntityOperator.setPageId(dataEntity, pageId, isChange);
        DataEntityOperator.setHashCode(dataEntity, object.hashCode());
    }

    /**
     * 获取页面id
     */
    public static String getPageId(Object object) {
        return DataEntityOperator.getPageId(getDataEntity(object, false));
    }

    /**
     * 不建议使用
     * 这个操作很危险，可以直接操作dataEntity内部的customparams
     * 后期修改
     * @param object
     * @return
     */
    public static ConcurrentHashMap<String, Object> getAllCustoms(Object object) {
        DataEntity temp = getDataEntity(object, false);
        if (temp != null) {
            return temp.customParams;
        }
        return null;
    }

    /**
     * 设置自定义参数
     */
    public static void setCustomParams(Object object, Map<String, ?> customParams) {
        checkCustomParams(object);
        DataEntityOperator.setCustomParams(getDataEntity(object, true), customParams);
    }

    private static void checkCustomParams(Object object) {
        View view = VTreeUtilsKt.getView(object);
        if (view != null) {
            VTreeMap vTreeMap = VTreeManager.INSTANCE.getCurrentVTreeInfo();
            if (vTreeMap != null) {
                VTreeNode node = vTreeMap.getTreeMap().get(view);
                if (node != null) {
                    DataEntity viewEntity = getDataEntity(view, false);
                    if (viewEntity != null && viewEntity == node.getDataEntity()) {
                        String id = viewEntity.elementId;
                        if (id == null) {
                            id = viewEntity.pageId;
                        }
                        if (id != null) {
                            Object exposureFlag = viewEntity.innerParams.get(InnerKey.VIEW_RE_EXPOSURE_FLAG);
                            Object identifyObj = viewEntity.innerParams.get(InnerKey.VIEW_IDENTIFIER);
                            int exposure = 0;
                            String identify = "";
                            if (exposureFlag instanceof Integer) {
                                exposure = (int) exposureFlag;
                            }
                            if (identifyObj instanceof String) {
                                identify = (String) identifyObj;
                            }

                            int viewIdentify = Arrays.hashCode(new Object[]{id, exposure, identify});
                            if (viewIdentify != node.getUniqueCode()) {
                                DataBinder.setDataEntity(view, viewEntity.deepClone());
                            }
                        }
                    }
                }
            }
        }
    }

    public static void deepCloneData(Object object) {
        DataEntity dataEntity = getDataEntity(object, false);
        if (dataEntity != null) {
            DataBinder.setDataEntity(object, dataEntity.deepClone());
        }
    }

    /**
     * 设置自定义参数
     */
    public static void setCustomParams(Object object, String key, Object value) {
        checkCustomParams(object);
        DataEntityOperator.setCustomParams(getDataEntity(object, true), key, value);
    }

    /**
     * 移除自定义参数
     */
    public static void removeCustomParam(Object object, String key) {
        checkCustomParams(object);
        DataEntityOperator.removeCustomParam(getDataEntity(object, false), key);
    }

    /**
     * 清空自定义参数
     */
    public static void removeAllCustomParams(Object object) {
        checkCustomParams(object);
        DataEntityOperator.removeAllCustomParams(getDataEntity(object, false));
    }

    public static void setEventCallback(Object object, String[] eventKey, IViewDynamicParamsProvider provider) {
        if (object == null || eventKey == null || eventKey.length == 0 || provider == null) {
            return;
        }
        View view = VTreeUtilsKt.getView(object);
        DataEntity dataEntity = getDataEntity(view, true);
        Map<String, IViewDynamicParamsProvider> map = (Map<String, IViewDynamicParamsProvider>) view.getTag(R.id.key_data_event_params);
        if (map == null) {
            map = new HashMap<>();
            view.setTag(R.id.key_data_event_params, map);
        }
        for (String key : eventKey) {
            map.put(key, provider);
            DataEntityOperator.setEventCallback(dataEntity, key, provider);
        }
    }

    /**
     * 保存内部参数
     */
    public static void setInnerParam(Object object, String key, Object value) {
        DataEntityOperator.putInnerParam(getDataEntity(object, true), key, value);
    }

    /**
     * 用于重新曝光，每个view内部存了一个自增参数，一个node的 eque 和 hashcode也受这个影响
     * @param object
     */
    public static void increaseReExposureFlag(Object object) {
        DataEntity entity = getDataEntity(object, true);
        if (entity != null) {
            int flag = 0;
            Object temp = DataEntityOperator.getInnerParam(entity, InnerKey.VIEW_RE_EXPOSURE_FLAG);
            if (temp != null) {
                flag = (int) temp;
            }
            DataEntityOperator.putInnerParam(entity, InnerKey.VIEW_RE_EXPOSURE_FLAG, flag + 1);
        }
    }

    /**
     * 查询内部参数
     */
    @Nullable
    public static Object getInnerParam(Object object, String key) {
        return DataEntityOperator.getInnerParam(getDataEntity(object, false), key);
    }

    /**
     * 删除内部参数
     */
    public static void removeInnerParam(Object object, String key) {
        DataEntityOperator.removeInnerParam(getDataEntity(object, false), key);
    }

    public static void removeDataEntity(Object object) {
        DataBinder.removeDataEntity(object);
    }

    public static DataEntity getDataEntity(Object object, boolean needCreate) {

        DataEntity entity = DataBinder.getDataEntity(object);
        if (entity == null && needCreate) {
            entity = new DataEntity();
            DataBinder.setDataEntity(object, entity);
        }
        return entity;
    }

    public static boolean isAlertView(View view) {
        Object param = DataEntityOperator.getInnerParam(getDataEntity(view, false), InnerKey.VIEW_ALERT_FLAG);
        if (param instanceof Boolean) {
            return (boolean) param;
        }
        return false;
    }

    /**
     * 判断当前activity是否是透明activity
     * @param activity
     * @return
     */
    public static boolean isTransparentActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        Object dataEntity = activity.getWindow().getDecorView().getTag(R.id.key_data_package);
        if (dataEntity instanceof DataEntity) {
            Object param = ((DataEntity) dataEntity).innerParams.get(VIEW_TRANSPARENT_ACTIVITY);
            if (param instanceof Boolean) {
                return (boolean) param;
            }
        }
        return false;
    }
}
