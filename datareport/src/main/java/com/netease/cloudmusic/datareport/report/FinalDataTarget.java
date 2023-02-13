package com.netease.cloudmusic.datareport.report;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.netease.cloudmusic.datareport.event.EventDispatch;
import com.netease.cloudmusic.datareport.event.EventKey;
import com.netease.cloudmusic.datareport.operator.DataReport;
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider;
import com.netease.cloudmusic.datareport.report.data.FinalData;
import com.netease.cloudmusic.datareport.provider.IDynamicParamsProvider;
import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.data.ReusablePool;
import com.netease.cloudmusic.datareport.report.exception.ExceptionReporter;
import com.netease.cloudmusic.datareport.report.exception.PublicParamInvalidError;
import com.netease.cloudmusic.datareport.report.exception.UserParamInvalidError;
import com.netease.cloudmusic.datareport.report.refer.ReferManager;
import com.netease.cloudmusic.datareport.utils.ThreadUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.netease.cloudmusic.datareport.report.InnerReportKeyKt.CURRENT_EVENT_PARAMS;
import static com.netease.cloudmusic.datareport.report.InnerReportKeyKt.CURRENT_NODE_TEMP_KEY;
import static com.netease.cloudmusic.datareport.report.InnerReportKeyKt.ELEMENT_LIST;
import static com.netease.cloudmusic.datareport.report.InnerReportKeyKt.INNER_EVENT_CODE;
import static com.netease.cloudmusic.datareport.report.InnerReportKeyKt.PAGE_LIST;

/**
 * 消费FinalData的类
 * 1. 会使用用户设置的IReporter进行上报
 * 2. Debug模式下会输出日志
 */
public class FinalDataTarget {

    /**
     * 对数据处理上报
     * @param finalData
     */
    public static void handle(@Nullable final FinalData finalData) {
        handleInner(finalData, false, false);
    }

    /**
     * 针对页面曝光做上报处理
     * @param finalData
     * @param isRoot
     */
    public static void handleInPageExposure(FinalData finalData, boolean isRoot) {
        handleInner(finalData, true, isRoot);
    }

    private static void handleInner(@Nullable final FinalData finalData, boolean isPageExposure, boolean isRoot) {
        if (finalData == null) {
            return;
        }
        String event = (String) finalData.eventParams.get(INNER_EVENT_CODE);
        if (event == null) {
            event = "";
        }
        String finalEvent = event;
        if (finalEvent.equals(EventKey.PAGE_VIEW) || finalEvent.equals(EventKey.VIEW_CLICK)) {
            finalData.put(InnerReportKeyKt.PARAMS_MUTABLE_REFER_KEY, DataReport.getInstance().getMultiRefer());
        }
        ThreadUtils.runOnUiThread(() -> {
            final Map<String, Object> publicParams = new ArrayMap<>();
            addRealtimeExternalParams(publicParams); //添加全局参数
            checkPublicPatternMapKey(publicParams);
            changeFinalData(finalData);
            IDynamicParamsProvider provider = DataReportInner.getInstance().getDynamicParamsProvider();
            if (provider != null) {
                String eventAction = (String) finalData.eventParams.get(INNER_EVENT_CODE);
                if (eventAction != null) {
                    provider.setEventDynamicParams(eventAction, finalData.getEventParams());
                }
            }

            EventDispatch.INSTANCE.postRunnable(() -> {
                if (isPageExposure) {
                    ReferManager.INSTANCE.onPageViewFix(finalData, isRoot);
                }
                Map<String, Object> finalParams = DataReportInner.getInstance()
                        .getConfiguration()
                        .getFormatter()
                        .formatEvent(publicParams,
                                finalData.eventParams == null ? null : new HashMap<>(finalData.eventParams));
                DataReportInner.getInstance().getConfiguration().getReporter().report(finalEvent, finalParams);
                recycleObject(finalData);
            });
        });
    }

    private static void changeFinalData(@NonNull FinalData finalData){
        Map<String, Object> map = finalData.getEventParams();
        List<Map<String, Object>> elist = (List<Map<String, Object>>) map.get(ELEMENT_LIST);
        List<Map<String, Object>> plist = (List<Map<String, Object>>) map.get(PAGE_LIST);
        if (elist != null && elist.size() > 0) {
            for (Map<String, Object> node : elist) {
                updateNode(node, CURRENT_NODE_TEMP_KEY);
            }
            updateNode(elist.get(0), CURRENT_EVENT_PARAMS);
        }
        if (plist != null && plist.size() > 0) {
            for (Map<String, Object> node : plist) {
                updateNode(node, CURRENT_NODE_TEMP_KEY);
            }
            updateNode(plist.get(0), CURRENT_EVENT_PARAMS);
        }
    }

    private static void updateNode(Map<String, Object> node, String key) {
        WeakReference<Object> reference = (WeakReference<Object>) node.remove(key);
        if (reference != null) {
            getCustomParams(reference.get(), node);
        }
    }

    private static void getCustomParams(Object provider, Map<String, Object> itemMap) {
        if (provider instanceof IViewDynamicParamsProvider) {
            Map<String, Object> map = ((IViewDynamicParamsProvider) provider).getViewDynamicParams();
            checkCustomPatternMapKey(map);
            itemMap.putAll(map);
        }
    }

    private static void recycleObject(@NonNull final FinalData finalData) {
        finalData.reset();
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ReusablePool.recycle(finalData, ReusablePool.TYPE_FINAL_DATA_REUSE);
            }
        });
    }

    /**
     * 添加外部设置的全局参数
     */
    private static void addRealtimeExternalParams(Map<String, Object> output) {
        if (output == null) {
            return;
        }
        IDynamicParamsProvider eventDynamicParams = DataReportInner.getInstance().getDynamicParamsProvider();
        if (eventDynamicParams != null) {
            eventDynamicParams.setPublicDynamicParams(output);
        }
    }

    private static void checkPublicPatternMapKey(Map<String, ?> map) {
        Pattern pattern = DataReportInner.getInstance().getConfiguration().getPatternGlobalKey();
        if (pattern == null) {
            return;
        }
        for (String key : map.keySet()) {
            if (!pattern.matcher(key).matches()) {
                ExceptionReporter.INSTANCE.reportError(new PublicParamInvalidError(key));
            }
        }
    }

    private static void checkCustomPatternMapKey(Map<String, ?> map) {
        Pattern pattern = DataReportInner.getInstance().getConfiguration().getPatternCustomKey();
        if (pattern == null) {
            return;
        }
        for (String key : map.keySet()) {
            if (!pattern.matcher(key).matches()) {
                ExceptionReporter.INSTANCE.reportError(new UserParamInvalidError(key));
            }
        }
    }
}
