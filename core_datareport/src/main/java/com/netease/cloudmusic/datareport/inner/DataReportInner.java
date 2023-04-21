package com.netease.cloudmusic.datareport.inner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.ConfigurationWrapper;
import com.netease.cloudmusic.datareport.R;
import com.netease.cloudmusic.datareport.data.DataEntity;
import com.netease.cloudmusic.datareport.data.ReusablePool;
import com.netease.cloudmusic.datareport.event.ClickEventObserver;
import com.netease.cloudmusic.datareport.event.CustomEventObserver;
import com.netease.cloudmusic.datareport.event.EventConfig;
import com.netease.cloudmusic.datareport.event.EventKey;
import com.netease.cloudmusic.datareport.event.WebEventObserver;
import com.netease.cloudmusic.datareport.event.WebEventType;
import com.netease.cloudmusic.datareport.operator.IDataReport;
import com.netease.cloudmusic.datareport.operator.IViewOperator;
import com.netease.cloudmusic.datareport.event.EventTransferPolicy;
import com.netease.cloudmusic.datareport.policy.MenuNode;
import com.netease.cloudmusic.datareport.policy.ReferConsumeOption;
import com.netease.cloudmusic.datareport.policy.TransferType;
import com.netease.cloudmusic.datareport.policy.VirtualParentConfig;
import com.netease.cloudmusic.datareport.provider.IChildPageChangeCallback;
import com.netease.cloudmusic.datareport.provider.IEventCallback;
import com.netease.cloudmusic.datareport.provider.IExposureCallback;
import com.netease.cloudmusic.datareport.provider.INodeEventCallback;
import com.netease.cloudmusic.datareport.provider.IProcessUpdateAction;
import com.netease.cloudmusic.datareport.provider.IReferStrategy;
import com.netease.cloudmusic.datareport.provider.IViewEventCallback;
import com.netease.cloudmusic.datareport.provider.ProcessUpdateManager;
import com.netease.cloudmusic.datareport.report.InnerReportKeyKt;
import com.netease.cloudmusic.datareport.report.data.PageStepManager;
import com.netease.cloudmusic.datareport.report.exception.EventKeyInvalidError;
import com.netease.cloudmusic.datareport.report.exception.ExceptionReporter;
import com.netease.cloudmusic.datareport.report.exception.LogicalMountEndlessLoopError;
import com.netease.cloudmusic.datareport.report.refer.ReferManager;
import com.netease.cloudmusic.datareport.scroller.ScrollInfo;
import com.netease.cloudmusic.datareport.utils.ProcessUtils;
import com.netease.cloudmusic.datareport.utils.SPUtils;
import com.netease.cloudmusic.datareport.vtree.LayoutObserverManager;
import com.netease.cloudmusic.datareport.vtree.VTreeUtilsKt;
import com.netease.cloudmusic.datareport.vtree.bean.VTreeMap;
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode;
import com.netease.cloudmusic.datareport.vtree.logic.LogicMenuManager;
import com.netease.cloudmusic.datareport.vtree.logic.LogicViewManager;
import com.netease.cloudmusic.datareport.Configuration;
import com.netease.cloudmusic.datareport.app.AppEventReporter;
import com.netease.cloudmusic.datareport.policy.ReportPolicy;
import com.netease.cloudmusic.datareport.provider.IDynamicParamsProvider;
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider;

import com.netease.cloudmusic.datareport.event.CustomEventType;
import com.netease.cloudmusic.datareport.data.DataRWProxy;

import com.netease.cloudmusic.datareport.report.ExposureEventReport;
import com.netease.cloudmusic.datareport.inject.EventCollector;
import com.netease.cloudmusic.datareport.event.EventDispatch;
import com.netease.cloudmusic.datareport.vtree.page.ViewContainerBinder;
import com.netease.cloudmusic.datareport.scroller.ScrollableViewObserver;
import com.netease.cloudmusic.datareport.vtree.VTreeManager;
import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.utils.ReportUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import kotlin.Pair;

import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_ALERT_FLAG;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_ALERT_PRIORITY;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_AS_ROOT_PAGE;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_ELEMENT_EXPOSURE_END;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_EVENT_TRANSFER;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_EXPOSURE_MIN_RATE;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_EXPOSURE_MIN_TIME;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_IDENTIFIER;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_LOGIC_VISIBLE;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_POSITION;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_REPORT_POLICY;
import static com.netease.cloudmusic.datareport.inner.InnerKey.VIEW_VIRTUAL_PARENT_NODE;
import static com.netease.cloudmusic.datareport.policy.TransferPolicyType.TYPE_FIND_DOWN_OID;
import static com.netease.cloudmusic.datareport.policy.TransferPolicyType.TYPE_FIND_UP_OID;
import static com.netease.cloudmusic.datareport.policy.TransferPolicyType.TYPE_TARGET_VIEW;

/**
 * DataReport接口实现
 */
public class DataReportInner implements IDataReport {
    private static final String TAG = "DataReportInner";
    private static final String INIT_PROCESS_ACTION = "init_process_action";
    private static final String CURRENT_PROCESS_NAME = "current_process_name";

    private static final IProcessUpdateAction initProcessAction = (sharedPreferences, editor, values) -> {
        try {
            String addProcessName = values.getAsString(CURRENT_PROCESS_NAME);
            if (addProcessName == null) {
                return null;
            }
            values.remove(CURRENT_PROCESS_NAME);
            String listStr =  sharedPreferences.getString(SPUtils.ALL_PROCESS_KEY, "[]");
            JSONArray list = new JSONArray(listStr);
            if (!listStr.contains(addProcessName)) {
                list.put(addProcessName);
                editor.putString(SPUtils.ALL_PROCESS_KEY, list.toString());
                List<String> modifyList = new ArrayList<>();
                modifyList.add(SPUtils.ALL_PROCESS_KEY);
                return modifyList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    };

    public static void registerPreferenceAction() {
        ProcessUpdateManager.INSTANCE.registerAction(INIT_PROCESS_ACTION, initProcessAction);
    }

    private Configuration mConfiguration;

    public static DataReportInner getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private DataReportInner() {
        registerPreferenceAction();
    }

    private static class InstanceHolder {
        static final DataReportInner INSTANCE = new DataReportInner();
    }

    @Override
    public void startWithConfiguration(Application application, Configuration configuration) {
        mConfiguration = new ConfigurationWrapper(configuration == null ? Configuration.getDefault() : configuration);
        if (isDebugMode()) {
            Log.i(TAG, "startWithConfiguration: application =" + application + ", configuration =" + configuration);
        }
        if (application != null) {
            application.registerActivityLifecycleCallbacks(EventCollector.getInstance());
            ReportUtils.setContext(application);

            SPUtils.edit().forSyncAction(INIT_PROCESS_ACTION).putString(CURRENT_PROCESS_NAME, ProcessUtils.getCurrentProcessName()).apply();

            initiateComponent();
        } else {
            if (isDebugMode()) {
                throw new NullPointerException("Application = null");
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initiateComponent() {
        AppEventReporter.getInstance();
        ClickEventObserver clickEventObserver = ClickEventObserver.INSTANCE;
        ViewContainerBinder.getInstance();
        ScrollableViewObserver.getInstance();
        VTreeManager vTreeExposureManager = VTreeManager.INSTANCE;
        EventDispatch eventDispatch = EventDispatch.INSTANCE;
        ExposureEventReport viewExposure = ExposureEventReport.INSTANCE;
        LogicMenuManager logicMenuManager = LogicMenuManager.INSTANCE;
    }

    @Override
    public IViewOperator setCustomParams(Object object, Map<String, ?> map) {
        if (isDebugMode()) {
            Log.i(TAG, "setPageParams: object=" + object + ", pageParams=" + map);
        }
        String containsKey = InnerReportKeyKt.isContainsInnerKeysAndNullCheck((Map<String, Object>) map);
        if (containsKey != null) {
            reportCustomKeyError(containsKey);
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.setCustomParams(object, map);
        }
        return this;
    }

    @Override
    public IViewOperator setCustomParams(Object object, String key, Object value) {
        if (isDebugMode()) {
            Log.i(TAG, "setPageParams: object=" + object + ", key=" + key + ", value=" + value);
        }
        if (key == null) {
            return this;
        }
        if (value == null) {
            value = "";
        }
        String containsKey = InnerReportKeyKt.isContainsInnerKeys(key);
        if (containsKey != null) {
            reportCustomKeyError(containsKey);
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.setCustomParams(object, key, value);
        }
        return this;
    }

    public void reportCustomKeyError(String key) {
        String errorMessage = "设置的参数的key和sdk内部的key冲突了, 冲突的key是：" + key;
        if (isDebugMode()) {
            throw new RuntimeException(errorMessage);
        } else {
            Log.e(TAG, errorMessage);
        }
    }

    @Override
    public IViewOperator setDynamicParams(Object object, IViewDynamicParamsProvider provider) {
        if (isDebugMode()) {
            Log.i(TAG, "setDynamicParams: object=" + object + ", provider=" + provider);
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.setViewDynamicParam(object, provider);
        }
        return this;
    }

    @Override
    public IViewOperator setPageId(Object object, String pageId) {
        if (isDebugMode()) {
            Log.i(TAG, "setPageId: object=" + object + ", pageId=" + pageId);
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.setPageId(object, pageId);
            VTreeManager.INSTANCE.onViewReport(object);
        }
        return this;
    }

    @Override
    public IViewOperator setElementId(Object object, String elementId) {
        if (isDebugMode()) {
            Log.i(TAG, "setElementId: object=" + object + ", elementId=" + elementId);
        }
        if (checkElementObjectArgument(object)) {
            DataRWProxy.setElementId(object, elementId);
            VTreeManager.INSTANCE.onViewReport(object);
        }
        return this;
    }

    @Override
    public IViewOperator clearOid(Object object) {
        DataRWProxy.setElementId(object, null);
        DataRWProxy.setPageId(object, null);
        VTreeManager.INSTANCE.onViewReport(object);
        return this;
    }

    @Override
    public IViewOperator setPosition(Object object, int position) {
        if (isDebugMode()) {
            Log.i(TAG, "setPosition: object=" + object + ", policy=" + position);
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.setInnerParam(object, InnerKey.VIEW_POSITION, position);
        }
        return this;
    }

    @Override
    public IViewOperator removeCustomParam(Object object, String key) {
        if (isDebugMode()) {
            Log.i(TAG, "removeCustomParam: object=" + object + ", key=" + key);
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.removeCustomParam(object, key);
        }
        return this;
    }

    @Override
    public IViewOperator resetCustomParams(Object object) {
        if (isDebugMode()) {
            Log.i(TAG, "resetPageParams: object=" + object);
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.removeAllCustomParams(object);
        }
        return this;
    }

    /**
     * 检验外部设置的元素object参数是不是合法的
     *
     * @param object 页面的object参数
     * @return true:合法；false:不合法
     */
    private boolean checkElementObjectArgument(Object object) {
        return object instanceof Dialog || object instanceof View || object instanceof MenuNode;
    }

    /**
     * 检验外部设置的track中object参数是不是合法的
     *
     * @param object object对象
     * @return true:合法；false:不合法
     */
    private boolean checkTrackObjectArgument(Object object) {
        return checkElementObjectArgument(object) || object instanceof Activity;
    }

    @Override
    public void reportEvent(EventConfig eventConfig) {
        if (isDebugMode()) {
            Log.i(TAG, "reportEvent: eventId=" +eventConfig.getEventId());
        }
        if (TextUtils.isEmpty(eventConfig.getEventId())) {
            ExceptionReporter.INSTANCE.reportError(new EventKeyInvalidError(""));
            return;
        }
        Pattern pattern = getConfiguration().getPatternCustomEvent();
        if (pattern != null && !pattern.matcher(eventConfig.getEventId()).matches()) {
            ExceptionReporter.INSTANCE.reportError(new EventKeyInvalidError(eventConfig.getEventId()));
            return;
        }
        if (isDebugMode() && eventConfig.getTargetObj() != null && DataRWProxy.getPageId(eventConfig.getTargetObj()) == null && DataRWProxy.getElementId(eventConfig.getTargetObj()) == null) {
            new AlertDialog.Builder(VTreeUtilsKt.getView(eventConfig.getTargetObj()).getContext()).setTitle("曙光埋点错误警告").setMessage("自定义上报错误，targetView没有设置oid！！！").show();
        }
        CustomEventObserver.INSTANCE.onCustomEvent(new CustomEventType(eventConfig));
    }

    @Nullable
    @Override
    public View getOidParents(View view) {
        View tempView = view;
        while (tempView != null) {
            if (hasOid(tempView)) {
                return tempView;
            }
            ViewParent parent = tempView.getParent();
            if (parent instanceof View) {
                tempView = (View) parent;
            } else {
                tempView = null;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public View getViewByOid(View view, String oid) {
        if (VTreeUtilsKt.getOid(view) == oid) {
            return view;
        }
        View target = VTreeUtilsKt.getParentByOid(view, oid);
        if (target != null) {
            return target;
        }
        return VTreeUtilsKt.getChildByOid(view, oid);
    }

    private boolean hasOid(View view) {
        if (DataRWProxy.getPageId(view) != null) {
            return true;
        }
        if (DataRWProxy.getElementId(view) != null) {
            return true;
        }
        return false;
    }

    @Override
    public String getSpmByView(View view) {
        VTreeMap vTreeMap = VTreeManager.INSTANCE.getCurrentVTreeInfo();
        if (vTreeMap != null) {
            VTreeNode node = vTreeMap.getTreeMap().get(view);
            if (node != null) {
                return node.getSpm();
            }
        }

        StringBuilder sb = new StringBuilder();
        View tempView = view;
        while (tempView != null) {
            String oid = DataRWProxy.getPageId(tempView);
            if (TextUtils.isEmpty(oid)) {
                oid = DataRWProxy.getElementId(tempView);
            }
            if (TextUtils.isEmpty(oid)) {
                ViewParent parent = tempView.getParent();
                if (parent instanceof View) {
                    tempView = (View) parent;
                } else {
                    tempView = null;
                }
                continue;
            }
            Object pos = DataRWProxy.getInnerParam(tempView, InnerKey.VIEW_POSITION);
            sb.append(oid);
            if (pos != null) {
                sb.append(":").append(pos);
            }
            sb.append("|");
            ViewParent parent = tempView.getParent();
            if (parent instanceof View) {
                tempView = (View) parent;
            } else {
                tempView = null;
            }
        }
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - 1);
    }

    public Pair<String, Boolean> getScmByViewForEr(View view) {
        VTreeMap vTreeMap = VTreeManager.INSTANCE.getCurrentVTreeInfo();
        if (vTreeMap != null) {
            VTreeNode node = vTreeMap.getTreeMap().get(view);
            if (node != null) {
                return node.getScmByEr();
            }
        }

        Boolean flag = false;
        IReferStrategy strategy = DataReportInner.getInstance().getConfiguration().getReferStrategy();
        if (strategy == null) {
            return new Pair<String, Boolean>("", flag);
        }
        StringBuilder sb = new StringBuilder();
        View tempView = view;
        while (tempView != null) {
            String oid = DataRWProxy.getPageId(tempView);
            if (TextUtils.isEmpty(oid)) {
                oid = DataRWProxy.getElementId(tempView);
            }
            if (TextUtils.isEmpty(oid)) {
                ViewParent parent = tempView.getParent();
                if (parent instanceof View) {
                    tempView = (View) parent;
                } else {
                    tempView = null;
                }
                continue;
            }
            Map<String, Object> map = DataRWProxy.getAllCustoms(tempView);
            Pair<String, Boolean> result = strategy.buildScm(map);
            sb.append(result.getFirst()).append("|");
            flag = flag || result.getSecond();
            ViewParent parent = tempView.getParent();
            if (parent instanceof View) {
                tempView = (View) parent;
            } else {
                tempView = null;
            }
        }
        if (sb.length() == 0) {
            return new Pair<>("", flag);
        }
        return new Pair<>(sb.substring(0, sb.length() - 1), flag);
    }

    public String getScmByView(View view) {
        return getScmByViewForEr(view).getFirst();
    }

    @Override
    public String getHsRefer() {
        return ReferManager.INSTANCE.getHsRefer();
    }

    @Override
    public String getSideRefer() {
        return ReferManager.INSTANCE.getSidRefer();
    }

    @MainThread
    @Override
    public String getRefer(Object view) {
        return ReferManager.INSTANCE.getReferOnly(view);
    }

    @Nullable
    @Override
    public String getLastRefer() {
        return ReferManager.INSTANCE.getLastRefer();
    }

    @Nullable
    @Override
    public String getReferByEvent(String event) {
        return ReferManager.INSTANCE.getReferByEvent(event);
    }

    @Nullable
    @Override
    public String getUndefineRefer(@Nullable String event) {
        return ReferManager.INSTANCE.getUndefineRefer(event);
    }

    @Nullable
    @Override
    public String getLastUndefineRefer() {
        return ReferManager.INSTANCE.getLastUndefineRefer();
    }

    @Override
    public String getMutableRefer() {
        return ReferManager.INSTANCE.getMutableRefer();
    }

    @Override
    public int getCurrentPageStep() {
        return PageStepManager.INSTANCE.getCurrentPageStep();
    }

    @Override
    public void onWebViewEvent(View webView, String event, String referFromWeb) {
    }

    @Override
    public void onWebReport(View webView, String eventCode, boolean useForRefer, JSONArray pList, JSONArray eList, JSONObject params, String spmPosKey) {
        if (isDebugMode()) {
            Log.i(TAG, "onWebReport: eventId=" +eventCode);
        }
        if (TextUtils.isEmpty(eventCode)) {
            ExceptionReporter.INSTANCE.reportError(new EventKeyInvalidError(""));
            return;
        }
        Pattern pattern = getConfiguration().getPatternCustomEvent();
        if (pattern != null && !pattern.matcher(eventCode).matches()) {
            ExceptionReporter.INSTANCE.reportError(new EventKeyInvalidError(eventCode));
            return;
        }
        View nodeView = getOidParents(webView);
        if (nodeView != null) {
            WebEventObserver.INSTANCE.onWebEvent(new WebEventType(nodeView, eventCode, useForRefer, pList, eList, params, spmPosKey));
        }
    }

    @Override
    public void onWebViewLog(View webView, String event, JSONObject params) {
    }

    @Override
    public IViewOperator setReportPolicy(Object object, ReportPolicy policy) {
        if (isDebugMode()) {
            Log.i(TAG, "setReportPolicy: object=" + object + ", policy=" + policy.name());
        }
        if (checkTrackObjectArgument(object)) {
            DataRWProxy.setInnerParam(object, InnerKey.VIEW_REPORT_POLICY, policy);
            VTreeManager.INSTANCE.onViewReport(object);
        }
        return this;
    }

    @Override
    public IViewOperator deepCloneData(Object object) {
        DataRWProxy.deepCloneData(object);
        return this;
    }

    public ReportPolicy getReportPolicy(Object object) {
        if (isDebugMode()) {
            Log.d(TAG, "getReportPolicy: ");
        }
        if (checkTrackObjectArgument(object)) {
            Object innerParam = DataRWProxy.getInnerParam(object, InnerKey.VIEW_REPORT_POLICY);
            if (innerParam instanceof ReportPolicy) {
                return (ReportPolicy) innerParam;
            }
        }
        return null;
    }

    /**
     * 设置页面的逻辑父亲
     * 这个时候有个问题，设置自己的逻辑父亲本身是自己的后代，会引起死循环，需要验证
     */
    @Override
    public IViewOperator setLogicParent(Object child, Object logicParent) {
        View childView = VTreeUtilsKt.getView(child);
        View logicView;
        if (logicParent instanceof String) {
            logicView = VTreeUtilsKt.getViewBySpm((String) logicParent);
        } else {
            logicView = VTreeUtilsKt.getView(logicParent);
        }

        if (isDebugMode()) {
            Log.i(TAG, "setLogicParent: child = " + child + ", logicParent = " + logicParent);
        }
        if (childView == null || logicView == null) {
            return this;
        }

        if (checkLogicError(childView, logicView)) {
            ExceptionReporter.INSTANCE.reportError(new LogicalMountEndlessLoopError(child, logicParent));
            return this;
        }

        WeakReference<View> old = (WeakReference<View>) DataRWProxy.getInnerParam(childView, InnerKey.LOGIC_PARENT);
        if (old != null && old.get() == logicView) {
            return this;
        }

        DataRWProxy.setInnerParam(childView, InnerKey.LOGIC_PARENT, new WeakReference<>(logicView));
        List<WeakReference<View>> list = (List<WeakReference<View>>) DataRWProxy.getInnerParam(logicView, InnerKey.LOGIC_CHILDREN);
        if (list == null) {
            list = new ArrayList<>();
            DataRWProxy.setInnerParam(logicView, InnerKey.LOGIC_CHILDREN, list);
        }
        boolean isIn = false;
        for (WeakReference<View> weakView : list) {
            if (weakView.get() == childView) {
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            list.add(new WeakReference<>(childView));
        }
        VTreeManager.INSTANCE.onViewReport(childView);
        return this;
    }

    /**
     * 检查逻辑挂靠的父是否实际上是逻辑挂靠子的后代
     */
    private boolean checkLogicError(View child, View logicParent) {
        ViewParent viewParent = logicParent.getParent();
        while (viewParent instanceof View) {
            if (viewParent == child) {
                return true;
            }
            viewParent = ((View)viewParent).getParent();
        }
        return false;
    }

    @Override
    public IViewOperator deleteLogicParent(Object object) {
        View child = VTreeUtilsKt.getView(object);
        if (isDebugMode()) {
            Log.i(TAG, "deleteLogicParent: child = " + child);
        }
        if (child == null) {
            return this;
        }
        WeakReference<View> old = (WeakReference<View>) DataRWProxy.getInnerParam(child, InnerKey.LOGIC_PARENT);
        if (old == null) {
            return this;
        }
        View parentView = old.get();
        if (parentView == null) {
            return this;
        }
        DataRWProxy.removeInnerParam(child, InnerKey.LOGIC_PARENT);
        List<WeakReference<View>> list = (List<WeakReference<View>>) DataRWProxy.getInnerParam(parentView, InnerKey.LOGIC_CHILDREN);
        if (list == null || list.isEmpty()) {
            return this;
        }
        int index = 0;
        for (; index < list.size(); index++) {
            if (list.get(index).get() == child) {
                break;
            }
        }
        if (index < list.size()) {
            list.remove(index);
        }
        VTreeManager.INSTANCE.onViewReport(child);
        return this;
    }

    @Override
    public IViewOperator setReuseIdentifier(Object object, String identifier) {
        if (isDebugMode()) {
            Log.i(TAG, "setReuseIdentifier: object = " + object + ", identifier = " + identifier);
        }
        if (object == null) {
            return this;
        }
        DataRWProxy.setInnerParam(object, InnerKey.VIEW_IDENTIFIER, identifier);
        return this;
    }

    @Override
    public IViewOperator setVisibleMargin(Object view, int left, int top, int right, int bottom) {
        if (isDebugMode()) {
            Log.i(TAG, "setVisibleMargin: view = " + view + ", left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
        }
        if (view == null) {
            return this;
        }
        DataRWProxy.setInnerParam(view, InnerKey.VIEW_VISIBLE_MARGIN, new Rect(left, top, right, bottom));
        VTreeManager.INSTANCE.onViewReport(view);
        return this;
    }

    @Override
    public IViewOperator setToOid(Object view, String ... oid) {
        if (isDebugMode()) {
            Log.i(TAG, "setToOid: view = " + view + ", oid = " + oid);
        }
        if (view == null) {
            return this;
        }
        DataRWProxy.setInnerParam(view, InnerKey.VIEW_TO_OID, Arrays.asList(oid));
        return this;
    }

    @Override
    public IViewOperator reExposureView(Object ... views) {
        if (isDebugMode()) {
            Log.i(TAG, "reExposureView");
        }
        if (views == null || views.length == 0) {
            return this;
        }
        for (Object view : views) {
            DataRWProxy.increaseReExposureFlag(view);
        }
        VTreeManager.INSTANCE.onViewReport(views[0]);
        return this;
    }

    @Override
    public IViewOperator setViewAsAlert(Object obj, boolean isAlertView, int priority) {
        View view = VTreeUtilsKt.getView(obj);
        if (isDebugMode()) {
            Log.i(TAG, "setViewAsAlert");
        }
        if (view == null) {
            return this;
        }
        DataRWProxy.setInnerParam(view, VIEW_ALERT_FLAG, isAlertView);
        DataRWProxy.setInnerParam(view, VIEW_ALERT_PRIORITY, priority);

        LogicViewManager.INSTANCE.setAlertView(view, isAlertView);
        VTreeManager.INSTANCE.onViewReport(view);
        return this;
    }

    @Override
    public IViewOperator reBuildVTree(@NonNull Object view) {
        VTreeManager.INSTANCE.onViewReport(view);
        return this;
    }

    @Override
    public IViewOperator setViewAsRootPage(@NonNull Object view, boolean flag) {
        DataRWProxy.setInnerParam(view, VIEW_AS_ROOT_PAGE, flag);
        LogicViewManager.INSTANCE.setRootView(VTreeUtilsKt.getView(view));
        return this;
    }

    @Override
    public IViewOperator setVirtualParentNode(@NotNull Object view, String elementId, String identifier, @Nullable VirtualParentConfig config) {
        return setVirtualParentNode(view, elementId, identifier, config, false);
    }

    @Override
    public IViewOperator setVirtualParentPageNode(@NotNull Object view, String pageId, String identifier, @Nullable VirtualParentConfig config) {
        return setVirtualParentNode(view, pageId, identifier, config, true);
    }


    private IViewOperator setVirtualParentNode(@NotNull Object view, String id, String identifier, @Nullable VirtualParentConfig config, boolean isPage) {
        VTreeNode node = ReusablePool.obtainVTreeNode(null, true, true);

        DataEntity dataEntity = new DataEntity();
        if (isPage) {
            dataEntity.pageId = id;
        } else {
            dataEntity.elementId = id;
        }

        if (identifier != null) {
            dataEntity.innerParams.put(VIEW_IDENTIFIER, identifier);
        }
        if (config != null) {
            Map<String, Object> params = config.getParams();
            if (params != null) {
                dataEntity.customParams.putAll(params);
            }
            Integer position = config.getPosition();
            if (position != null) {
                dataEntity.innerParams.put(VIEW_POSITION, position);
            }
            ReportPolicy policy = config.getReportPolicy();
            if (policy != null) {
                dataEntity.innerParams.put(VIEW_REPORT_POLICY, policy);
            }
            Boolean exposureEndEnable = config.getExposureEndEnable();
            if (exposureEndEnable != null) {
                dataEntity.innerParams.put(VIEW_ELEMENT_EXPOSURE_END, exposureEndEnable);
            }
        }

        node.setData(id, isPage, dataEntity);
        node.setInnerParams(dataEntity.innerParams);

        VTreeNode tempNode = (VTreeNode) DataRWProxy.getInnerParam(view, VIEW_VIRTUAL_PARENT_NODE);
        DataRWProxy.setInnerParam(view, VIEW_VIRTUAL_PARENT_NODE, node);
        VTreeManager.INSTANCE.onViewReport(view);
        return this;
    }

    @Override
    public IViewOperator clearVirtualParentNode(@NonNull Object view) {
        DataRWProxy.removeInnerParam(view, VIEW_VIRTUAL_PARENT_NODE);
        VTreeManager.INSTANCE.onViewReport(view);
        return this;
    }

    @Override
    public IViewOperator setLogicVisible(@NonNull Object view, boolean isVisible) {
        DataRWProxy.setInnerParam(view, VIEW_LOGIC_VISIBLE, isVisible);
        VTreeManager.INSTANCE.onViewReport(view);
        return this;
    }

    @Override
    public IViewOperator setExposureMinTime(@NonNull Object view, long time) {
        DataRWProxy.setInnerParam(view, VIEW_EXPOSURE_MIN_TIME, time);
        return this;
    }

    @Override
    public IViewOperator setExposureMinRate(@NonNull Object view, float rate) {
        DataRWProxy.setInnerParam(view, VIEW_EXPOSURE_MIN_RATE, rate);
        return this;
    }

    @Override
    public IViewOperator setExposureCallback(@NonNull Object view, IExposureCallback callback) {
        DataRWProxy.setExposureCallback(view, callback);
        return this;
    }

    @Override
    public IViewOperator setElementExposureEnd(Object view, boolean enable) {
        DataRWProxy.setInnerParam(view, VIEW_ELEMENT_EXPOSURE_END, enable);
        return this;
    }

    @Override
    public IViewOperator addEventParamsCallback(Object view, String[] eventIds, IViewDynamicParamsProvider provider) {
        DataRWProxy.setEventCallback(view, eventIds, provider);
        return this;
    }

    @Override
    public IViewOperator setClickParamsCallback(Object view, IViewDynamicParamsProvider provider) {
        addEventParamsCallback(view, new String[]{EventKey.VIEW_CLICK}, provider);
        return this;
    }

    @Override
    public IViewOperator setEventTransferPolicy(Object view, @TransferType int type, @Nullable View targetView, @Nullable String targetOid) {
        if (type == TYPE_FIND_DOWN_OID || type == TYPE_FIND_UP_OID || type == TYPE_TARGET_VIEW) {
            EventTransferPolicy policy = new EventTransferPolicy(type, targetOid, targetView);
            DataRWProxy.setInnerParam(view, VIEW_EVENT_TRANSFER, policy);
        }
        return this;
    }

    @Override
    public IViewOperator setScrollEventEnable(View view, boolean enable) {
        ScrollInfo info = (ScrollInfo) view.getTag(R.id.key_scroll_id);
        if (info == null) {
            info = new ScrollInfo();
            info.setScrollEventEnable(enable);
            view.setTag(R.id.key_scroll_id, info);
        }
        return this;
    }

    @Override
    public IViewOperator setEnableLayoutObserver(Object view, boolean enable) {
        DataRWProxy.setInnerParam(view, InnerKey.VIEW_ENABLE_LAYOUT_OBSERVER, enable);
        return this;
    }

    @Override
    public IViewOperator setTransparentActivity(Activity activity, boolean isTransparent) {
        DataRWProxy.setInnerParam(activity, InnerKey.VIEW_TRANSPARENT_ACTIVITY, isTransparent);
        return this;
    }

    @Override
    public IViewOperator setIgnoreActivity(Activity activity, boolean isIgnore) {
        DataRWProxy.setInnerParam(activity, InnerKey.VIEW_IGNORE_ACTIVITY, isIgnore);
        return this;
    }

    @Override
    public IViewOperator setNodeIgnoreRefer(Object view, boolean enable) {
        DataRWProxy.setInnerParam(view, InnerKey.VIEW_IGNORE_REFER, enable);
        return this;
    }

    @Override
    public IViewOperator setIgnoreChildPage(Object view) {
        DataRWProxy.setInnerParam(view, InnerKey.VIEW_IGNORE_CHILD_PAGE, true);
        return this;
    }

    @Override
    public IViewOperator setReferMute(Object view, boolean enable) {
        DataRWProxy.setInnerParam(view, InnerKey.VIEW_REFER_MUTE, enable);
        return this;
    }

    @Override
    public IViewOperator setSubPageGenerateReferEnable(Object view, boolean enable) {
        DataRWProxy.setInnerParam(view, InnerKey.VIEW_GENERATE_REFER_ENABLE, enable);
        return this;
    }

    @Override
    public IViewOperator setSubPageConsumeReferOption(Object view, ReferConsumeOption option) {
        switch (option) {
            case CONSUME_NONE:
                DataRWProxy.removeInnerParam(view, InnerKey.VIEW_REFER_CONSUME_OPTION);
            case CONSUME_EVENT_REFER:
                DataRWProxy.setInnerParam(view, InnerKey.VIEW_REFER_CONSUME_OPTION, InnerKey.REFER_CONSUME_OPTION_EVENT);
            case CONSUME_SUB_PAGE_REFER:
                DataRWProxy.setInnerParam(view, InnerKey.VIEW_REFER_CONSUME_OPTION, InnerKey.REFER_CONSUME_OPTION_SUB_PAGE);
            case CONSUME_ALL:
                DataRWProxy.setInnerParam(view, InnerKey.VIEW_REFER_CONSUME_OPTION, InnerKey.REFER_CONSUME_OPTION_EVENT | InnerKey.REFER_CONSUME_OPTION_SUB_PAGE);
        }
        return this;
    }

    @Override
    public void openLayoutObserver(View view, Boolean flag) {
        if (flag) {
            LayoutObserverManager.INSTANCE.openObserver(view);
        } else {
            LayoutObserverManager.INSTANCE.closeObserver(view);
        }
    }

    @Override
    public Integer getInnerPosition(Object obj) {
        Object pos = DataRWProxy.getInnerParam(obj, InnerKey.VIEW_POSITION);
        if (pos instanceof Integer) {
            return (Integer) pos;
        }
        return null;
    }

    @Override
    public void addEventCallback(IEventCallback callback) {
        EventDispatch.INSTANCE.addEventCallback(callback);
    }

    @Override
    public void addViewEventCallback(IViewEventCallback callback) {
        EventDispatch.INSTANCE.addViewEventCallback(callback);
    }

    @Override
    @MainThread
    public void removeViewEventCallback(IViewEventCallback callback){
        EventDispatch.INSTANCE.removeViewEventCallback(callback);
    }

    @Override
    public void addNodeEventCallback(INodeEventCallback callback) {
        EventDispatch.INSTANCE.addNodeEventCallback(callback);
    }

    @Override
    public void removeNodeEventCallback(INodeEventCallback callback) {
        EventDispatch.INSTANCE.removeNodeEventCallback(callback);
    }

    @Override
    public String getCurrentRootPageOid() {
        return VTreeManager.INSTANCE.getRootPageOid();
    }

    @Override
    public String getCompleteRefers() {
        try {
            JSONArray jsonArray = VTreeManager.INSTANCE.getMutableReferArray();
            String eventRefer = ReferManager.INSTANCE.getLastRefer();
            if (eventRefer == null) {
                eventRefer = "";
            }
            StringBuilder sb = new StringBuilder(eventRefer);
            for (int i = 0; i < jsonArray.length(); i++) {
                sb.append(",").append(jsonArray.getString(i));
            }
            if (sb.length() == 0) {
                return ",";
            } else {
                return sb.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return ",";
        }
    }

    @Nullable
    @Override
    public String getSessionId() {
        return AppEventReporter.getInstance().getCurrentSessionId();
    }

    @Override
    public String getRootPageSpm() {
        return VTreeManager.INSTANCE.getRootPageSpm();
    }

    @Override
    public String getChildPageSpm() {
        return VTreeManager.INSTANCE.getChildPageSpm();
    }

    @Nullable
    @Override
    public String getChildPageOid() {
        return VTreeManager.INSTANCE.getChildPageOid();
    }

    @Override
    public void registerChildPageOidChangeCallback(IChildPageChangeCallback callback) {
        EventDispatch.INSTANCE.addChildPageChangeCallback(callback);
    }

    @Override
    public boolean isProcessForeground() {
        return AppEventReporter.getInstance().isCurrentProcessAppForeground();
    }

    public Configuration getConfiguration() {
        if (mConfiguration == null) {
            return new ConfigurationWrapper(Configuration.getDefault());
        }
        return mConfiguration;
    }

    public IDynamicParamsProvider getDynamicParamsProvider() {
        return getConfiguration().getDynamicParamsProvider();
    }

    public boolean isDebugMode() {
        return getConfiguration().isDebugMode();
    }

    public boolean isDataCollectEnable() {
        return getConfiguration().isDataCollectEnable();
    }

}
