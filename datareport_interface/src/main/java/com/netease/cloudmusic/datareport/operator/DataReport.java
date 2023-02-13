package com.netease.cloudmusic.datareport.operator;

import android.app.Activity;
import android.app.Application;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.Configuration;
import com.netease.cloudmusic.datareport.event.EventConfig;
import com.netease.cloudmusic.datareport.policy.ReportPolicy;
import com.netease.cloudmusic.datareport.policy.TransferType;
import com.netease.cloudmusic.datareport.policy.VirtualParentConfig;
import com.netease.cloudmusic.datareport.provider.IExposureCallback;
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider;
import com.netease.cloudmusic.datareport.provider.IViewEventCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * 暴露给外部调用的API方法
 */
public class DataReport implements IDataReport {

    private IDataReport dataReport;

    private DataReport() {
    }

    private static class InstanceHolder {
        static final DataReport INSTANCE = new DataReport();
    }

    public static DataReport getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void init(IDataReport dataReport) {
        this.dataReport = dataReport;
    }

    public void init(IDataReport dataReport, Application application, Configuration configuration) {
        this.dataReport = dataReport;
        startWithConfiguration(application, configuration);
    }

    /**
     * 初始化配置
     *
     * @param application   应用对象
     * @param configuration 配置信息
     */
    @Override
    public void startWithConfiguration(Application application, Configuration configuration) {
        if (dataReport != null) {
            dataReport.startWithConfiguration(application, configuration);
        }
    }

    @Override
    public void reportEvent(EventConfig eventConfig) {
        if (dataReport != null) {
            dataReport.reportEvent(eventConfig);
        }
    }

    @Nullable
    @Override
    public View getOidParents(View view) {
        if (dataReport != null) {
            return dataReport.getOidParents(view);
        }
        return null;
    }

    @Nullable
    @Override
    public View getViewByOid(View view, String oid) {
        if (dataReport != null) {
            return dataReport.getViewByOid(view, oid);
        }
        return null;
    }

    @Override
    public String getSpmByView(View view) {
        if (dataReport != null) {
            return dataReport.getSpmByView(view);
        }
        return "";
    }

    @Override
    public String getScmByView(View view) {
        if (dataReport != null) {
            return dataReport.getScmByView(view);
        }
        return "";
    }

    @Override
    public String getHsRefer() {
        if (dataReport != null) {
            return dataReport.getHsRefer();
        }
        return "";
    }

    @Override
    public String getSideRefer() {
        if (dataReport != null) {
            return dataReport.getSideRefer();
        }
        return "";
    }

    @MainThread
    @Override
    public String getRefer(Object view) {
        if (dataReport != null) {
            return dataReport.getRefer(view);
        }
        return "";
    }

    @Nullable
    @Override
    public String getLastRefer() {
        if (dataReport != null) {
            return dataReport.getLastRefer();
        }
        return null;
    }

    @Nullable
    @Override
    public String getReferByEvent(String event) {
        if (dataReport != null) {
            return dataReport.getReferByEvent(event);
        }
        return null;
    }

    @Nullable
    @Override
    public String getUndefineRefer(@Nullable String event) {
        if (dataReport != null) {
            return dataReport.getUndefineRefer(event);
        }
        return null;
    }

    @Nullable
    @Override
    public String getLastUndefineRefer() {
        if (dataReport != null) {
            return dataReport.getLastUndefineRefer();
        }
        return null;
    }

    @Override
    public String getMultiRefer() {
        if (dataReport != null) {
            return dataReport.getMultiRefer();
        }
        return "";
    }

    @Override
    public int getCurrentPageStep() {
        if (dataReport != null) {
            return dataReport.getCurrentPageStep();
        }
        return 0;
    }

    @Override
    public void onWebViewEvent(View webView, String event, String referFromWeb) {
        if (dataReport != null) {
            dataReport.onWebViewEvent(webView, event, referFromWeb);
        }
    }

    @Override
    public void onWebReport(View webView, String eventCode, boolean useForRefer, JSONArray pList, JSONArray eList, JSONObject params, String spmPosKey) {
        if (dataReport != null) {
            dataReport.onWebReport(webView, eventCode, useForRefer, pList, eList, params, spmPosKey);
        }
    }

    @Override
    public void onWebViewLog(View webView, String event, JSONObject params) {
        if (dataReport != null) {
            dataReport.onWebViewLog(webView, event, params);
        }
    }

    @Override
    public IViewOperator setCustomParams(Object object, Map<String, ?> map) {
        if (dataReport != null) {
            dataReport.setCustomParams(object, map);
        }
        return this;
    }

    @Override
    public IViewOperator setCustomParams(Object object, String key, Object value) {
        if (dataReport != null) {
            dataReport.setCustomParams(object, key, value);
        }
        return this;
    }

    @Override
    public IViewOperator setDynamicParams(Object object, IViewDynamicParamsProvider provider) {
        if (dataReport != null) {
            dataReport.setDynamicParams(object, provider);
        }
        return this;
    }

    /**
     * 设置页面所对应的pageId
     *
     * @param object Activity/Dialog/View
     * @param pageId pageId
     */
    @Override
    public IViewOperator setPageId(Object object, String pageId) {
        if (dataReport != null) {
            dataReport.setPageId(object, pageId);
        }
        return this;
    }

    /**
     * 设置元素所对应的elementId
     *
     * @param object    Dialog/View
     * @param elementId elementId
     */
    @Override
    public IViewOperator setElementId(Object object, String elementId) {
        if (dataReport != null) {
            dataReport.setElementId(object, elementId);
        }
        return this;
    }

    @Override
    public IViewOperator clearOid(Object object) {
        if (dataReport != null) {
            dataReport.clearOid(object);
        }
        return this;
    }

    @Override
    public IViewOperator setPosition(Object object, int pos) {
        if (dataReport != null) {
            dataReport.setPosition(object, pos);
        }
        return this;
    }

    @Override
    public IViewOperator removeCustomParam(Object object, String key) {
        if (dataReport != null) {
            dataReport.removeCustomParam(object, key);
        }
        return this;
    }

    @Override
    public IViewOperator resetCustomParams(Object object) {
        if (dataReport != null) {
            dataReport.resetCustomParams(object);
        }
        return this;
    }

    @Override
    public IViewOperator setReportPolicy(Object object, ReportPolicy policy) {
        if (dataReport != null) {
            dataReport.setReportPolicy(object, policy);
        }
        return this;
    }

    /**
     * 设置一个视图的逻辑父亲
     */
    @Override
    public IViewOperator setLogicParent(Object object, Object logicParent) {
        if (dataReport != null) {
            dataReport.setLogicParent(object, logicParent);
        }
        return this;
    }

    @Override
    public IViewOperator deleteLogicParent(Object child) {
        if (dataReport != null) {
            dataReport.deleteLogicParent(child);
        }
        return this;
    }

    @Override
    public IViewOperator setReuseIdentifier(Object object, String identifier) {
        if (dataReport != null) {
            dataReport.setReuseIdentifier(object, identifier);
        }
        return this;
    }

    @Override
    public IViewOperator setVisibleMargin(Object view, int left, int top, int right, int bottom) {
        if (dataReport != null) {
            dataReport.setVisibleMargin(view, left, top, right, bottom);
        }
        return this;
    }

    @Override
    public IViewOperator setToOid(Object view, String... oid) {
        if (dataReport != null) {
            dataReport.setToOid(view, oid);
        }
        return this;
    }

    @Override
    public IViewOperator reExposureView(Object... views) {
        if (dataReport != null) {
            dataReport.reExposureView(views);
        }
        return this;
    }

    @Override
    public IViewOperator setViewAsAlert(Object view, boolean isAlertView, int priority) {
        if (dataReport != null) {
            dataReport.setViewAsAlert(view, isAlertView, priority);
        }
        return this;
    }

    @Override
    public IViewOperator reBuildVTree(@Nullable Object view) {
        if (dataReport != null) {
            dataReport.reBuildVTree(view);
        }
        return this;
    }

    @Override
    public IViewOperator setViewAsRootPage(@NonNull Object view, boolean flag) {
        if (dataReport != null) {
            dataReport.setViewAsRootPage(view, flag);
        }
        return this;
    }

    @Override
    public IViewOperator setVirtualParentNode(@NonNull Object view, String elementId, String identifier, @Nullable VirtualParentConfig config) {
        if (dataReport != null) {
            dataReport.setVirtualParentNode(view, elementId, identifier, config);
        }
        return this;
    }

    @Override
    public IViewOperator clearVirtualParentNode(@NonNull Object view) {
        if (dataReport != null) {
            dataReport.clearVirtualParentNode(view);
        }
        return this;
    }

    @Override
    public IViewOperator setLogicVisible(@NonNull Object view, boolean isVisible) {
        if (dataReport != null) {
            dataReport.setLogicVisible(view, isVisible);
        }
        return this;
    }

    @Override
    public IViewOperator setExposureMinTime(@NonNull Object view, long time) {
        if (dataReport != null) {
            dataReport.setExposureMinTime(view, time);
        }
        return this;
    }

    @Override
    public IViewOperator setExposureMinRate(@NonNull Object view, float rate) {
        if (dataReport != null) {
            dataReport.setExposureMinRate(view, rate);
        }
        return this;
    }

    @Override
    public IViewOperator setExposureCallback(@NonNull Object view, IExposureCallback callback) {
        if (dataReport != null) {
            dataReport.setExposureCallback(view, callback);
        }
        return this;
    }

    @Override
    public IViewOperator setElementExposureEnd(Object view, boolean enable) {
        if (dataReport != null) {
            dataReport.setElementExposureEnd(view, enable);
        }
        return this;
    }

    @Override
    public IViewOperator addEventParamsCallback(Object view, String[] eventIds, IViewDynamicParamsProvider provider) {
        if (dataReport != null) {
            dataReport.addEventParamsCallback(view, eventIds, provider);
        }
        return this;
    }

    @Override
    public IViewOperator setClickParamsCallback(Object view, IViewDynamicParamsProvider provider) {
        if (dataReport != null) {
            dataReport.setClickParamsCallback(view, provider);
        }
        return this;
    }

    @Override
    public IViewOperator setEventTransferPolicy(Object view, @TransferType int type, @Nullable View targetView, @Nullable String targetOid) {
        if (dataReport != null) {
            dataReport.setEventTransferPolicy(view, type, targetView, targetOid);
        }
        return this;
    }

    @Override
    public IViewOperator setScrollEventEnable(View view, boolean enable) {
        if (dataReport != null) {
            dataReport.setScrollEventEnable(view, enable);
        }
        return this;
    }

    @Override
    public IViewOperator setEnableLayoutObserver(Object view, boolean enable) {
        if (dataReport != null) {
            dataReport.setEnableLayoutObserver(view, enable);
        }
        return this;
    }

    @Override
    public IViewOperator setTransparentActivity(Activity activity, boolean isTransparent) {
        if (dataReport != null) {
            dataReport.setTransparentActivity(activity, isTransparent);
        }
        return this;
    }

    @Override
    public IViewOperator setIgnoreActivity(Activity activity, boolean isIgnore) {
        if (dataReport != null) {
            dataReport.setIgnoreActivity(activity, isIgnore);
        }
        return this;
    }

    @Override
    public IViewOperator setNodeIgnoreRefer(Object view, boolean enable) {
        if (dataReport != null) {
            dataReport.setNodeIgnoreRefer(view, enable);
        }
        return this;
    }

    @Override
    public IViewOperator setIgnoreChildPage(Object view) {
        if (dataReport != null) {
            dataReport.setIgnoreChildPage(view);
        }
        return this;
    }

    @Override
    public IViewOperator setReferMute(Object view, boolean enable) {
        if (dataReport != null) {
            dataReport.setReferMute(view, enable);
        }
        return this;
    }

    @Override
    public void openLayoutObserver(View view, Boolean flag) {
        if (dataReport != null) {
            dataReport.openLayoutObserver(view, flag);
        }
    }

    @Override
    @Nullable
    public Integer getInnerPosition(Object obj) {
        if (dataReport != null) {
            return dataReport.getInnerPosition(obj);
        }
        return null;
    }

    @Override
    @MainThread
    public void addViewEventCallback(IViewEventCallback callback) {
        if (dataReport != null) {
            dataReport.addViewEventCallback(callback);
        }
    }

    @Override
    @MainThread
    public void removeViewEventCallback(IViewEventCallback callback) {
        if (dataReport != null) {
            dataReport.removeViewEventCallback(callback);
        }
    }

    @Nullable
    @Override
    public String getCurrentRootPageOid() {
        if (dataReport != null) {
            return dataReport.getCurrentRootPageOid();
        }
        return null;
    }

    @Nullable
    @Override
    public String getSessionId() {
        if (dataReport != null) {
            return dataReport.getSessionId();
        }
        return null;
    }

    @Override
    public String getRootPageSpm() {
        if (dataReport != null) {
            return dataReport.getRootPageSpm();
        }
        return null;
    }

    @Override
    public String getChildPageSpm() {
        if (dataReport != null) {
            return dataReport.getChildPageSpm();
        }
        return null;
    }

    @Nullable
    @Override
    public String getChildPageOid() {
        if (dataReport != null) {
            return dataReport.getChildPageOid();
        }
        return null;
    }

    @Override
    public boolean isProcessForeground() {
        if (dataReport != null) {
            return dataReport.isProcessForeground();
        }
        return false;
    }

    /**
     * 判断曙光埋点是否被初始化
     * @return
     */
    public boolean isInitDataReport(){
        return dataReport != null;
    }
}
