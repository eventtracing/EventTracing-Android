package com.netease.cloudmusic.datareport.operator;

import android.app.Application;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.Configuration;
import com.netease.cloudmusic.datareport.event.EventConfig;
import com.netease.cloudmusic.datareport.provider.IViewEventCallback;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 接口类，对外的公共接口
 */
public interface IDataReport extends IViewOperator {

    /**
     * 初始化配置
     *
     * @param application   上下文信息
     * @param configuration 配置信息
     */
    void startWithConfiguration(Application application, Configuration configuration);

    /**
     * 自定义事件上报
     * @param eventConfig 上报的配置
     */
    void reportEvent(EventConfig eventConfig);

    /**
     * view自己本身没有oid，但是想发自定义事件，可以通过这个方法找到离自己最近的有oid的祖宗
     * 如果view自己本身是有oid的就会返回自己
     * @param view 需要查找有oid的祖宗的view
     * @return 返回祖宗view， 如果view自己本身是有oid的就会返回自己
     */
    @Nullable
    View getOidParents(View view);


    /**
     * 从给的view的上下级关系找到相应的oid
     * 注意：这里的oid必须是view的上下级关系。随便给一个oid是筛不出来的
     * @param view
     * @return
     */
    @Nullable
    View getViewByOid(View view, String oid);

    /**
     * 获取一个View的spm
     */
    String getSpmByView(View view);

    /**
     * 获取一个View的scm
     */
    String getScmByView(View view);

    String getHsRefer();

    String getSideRefer();

    /**
     * 获取一个View对应的refer
     */
    @MainThread
    @Nullable
    String getRefer(Object view);

    @MainThread
    @Nullable
    String getLastRefer();

    @MainThread
    @Nullable
    String getReferByEvent(String event);

    @MainThread
    @Nullable
    String getUndefineRefer(@Nullable String event);

    @MainThread
    @Nullable
    String getLastUndefineRefer();

    String getMultiRefer();

    /**
     * 获取当前的pageStep
     * @return
     */
    int getCurrentPageStep();

    /**
     * 容器的事件产生
     * @param webView
     * @param event
     * @param referFromWeb
     */
    @Deprecated
    void onWebViewEvent(View webView, String event, String referFromWeb);

    /**
     * web页面数据上报
     * @param webView
     * @param eventCode
     * @param useForRefer
     * @param pList
     * @param eList
     * @param params
     */
    void onWebReport(View webView, String eventCode, boolean useForRefer, JSONArray pList, JSONArray eList, JSONObject params, String spmPosKey);

    /**
     * 容器的日志上报
     * @param webView
     * @param event
     * @param params
     */
    @Deprecated
    void onWebViewLog(View webView, String event, JSONObject params);

    /**
     * 给view标志上 GlobalLayoutObserver的时候就会在VTree上从上往下找到第一个标志，然后局部遍历树
     * @param view view
     * @param flag true 会添加标志，false 会清除标志
     * @return
     */
    void openLayoutObserver(View view, Boolean flag);

    /**
     * 获取这个view的position，这个position是存放在 inner params的
     * @param obj 对象
     */
    @Nullable
    Integer getInnerPosition(Object obj);

    /**
     * 添加事件的回调
     * 注意： 内部会使用 weakReference 进行存储
     * @param callback 回调
     */
    @MainThread
    void addViewEventCallback(IViewEventCallback callback);

    @MainThread
    void removeViewEventCallback(IViewEventCallback callback);

    /**
     * 获取当前虚拟树的rootPage
     * 获取的算法是：找到虚拟节点下面的最后一个子节点作为 rootPage
     * @return
     */
    @Nullable
    String getCurrentRootPageOid();

    /**
     * 获取当前的sessionId
     * @return
     */
    @Nullable
    String getSessionId();


    /**
     * 获取当前虚拟树的rootPage
     * 获取的算法是：找到虚拟节点下面的最后一个子节点作为 rootPage
     * @return
     */
    String getRootPageSpm();

    /**
     * 获取当前虚拟树的最叶子子page
     * @return
     */
    String getChildPageSpm();

    /**
     * 获取当前虚拟树的最叶子子page的oid
     * 获取的算法是：找到虚拟节点下面的最后一个子节点作为 rootPage
     * @return
     */
    @Nullable
    String getChildPageOid();

    /**
     * 当前进程是否在前台
     * @return
     */
    boolean isProcessForeground();
}