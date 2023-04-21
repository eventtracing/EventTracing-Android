package com.netease.cloudmusic.datareport.report.data;

import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责用来记录页面的PageContext
 */
public class PageContextManager {

    public final Map<Integer, IContext> mContextMap = new ConcurrentHashMap<>();

    /**
     * 添加一个页面的来源页面信息
     */
    public void set(VTreeNode vTreeNode, Integer object, PageContext pageContext) {
        mContextMap.put(object, pageContext);
    }

    public IContext remove(VTreeNode vTreeNode, Integer object) {
        return mContextMap.remove(object);
    }

    /**
     * 获取一个页面的来源页面信息
     */
    public IContext get(Integer object) {
        return mContextMap.get(object);
    }

    /**
     * 清空所有页面的来源页面信息，场景是resetPagePath后
     */
    public void clear() {
        mContextMap.clear();
    }

    /**
     * 获取单例对象
     */
    public static PageContextManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private PageContextManager() {
    }

    private static class InstanceHolder {
        static final PageContextManager INSTANCE = new PageContextManager();
    }
}
