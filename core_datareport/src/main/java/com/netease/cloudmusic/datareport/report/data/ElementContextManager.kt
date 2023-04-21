package com.netease.cloudmusic.datareport.report.data

import java.util.concurrent.ConcurrentHashMap

object ElementContextManager {
    public val mContextMap: MutableMap<Int, IContext> = ConcurrentHashMap()

    /**
     * 添加一个页面的来源页面信息
     */
    operator fun set(obj: Int, elementContext: IContext) {
        mContextMap[obj] = elementContext
    }

    fun remove(obj: Int): IContext? {
        return mContextMap.remove(obj)
    }

    /**
     * 获取一个页面的来源页面信息
     */
    operator fun get(obj: Int?): IContext? {
        return mContextMap[obj]
    }

    /**
     * 清空所有页面的来源页面信息，场景是resetPagePath后
     */
    fun clear() {
        mContextMap.clear()
    }
}
