package com.netease.cloudmusic.datareport.vtree.logic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.MenuPopupWindow
import com.netease.cloudmusic.datareport.R
import com.netease.cloudmusic.datareport.data.DataEntity
import com.netease.cloudmusic.datareport.inject.EventCollector
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider
import java.util.*

/**
 * 针对系统的menu的埋点进行处理
 */
object LogicMenuManager : DefaultEventListener() {

    private val logicMenuObserverList = mutableListOf<LogicMenuObserver>()

    init {
        EventCollector.getInstance().registerEventListener(this)
    }

    fun addObserver(observer: LogicMenuObserver) {
        if (!logicMenuObserverList.contains(observer)) {
            logicMenuObserverList.add(observer)
        }
    }

    fun removeObserver(observer: LogicMenuObserver){
        logicMenuObserverList.remove(observer)
    }

    private var menuItemInfo: WeakHashMap<Activity, MutableMap<Int, DataEntity>> = WeakHashMap()
    private var menuPageInfo: WeakHashMap<Activity, MutableMap<Int, DataEntity>> = WeakHashMap()

    private var dynamicParamsList: WeakHashMap<Activity, MutableList<IViewDynamicParamsProvider>> = WeakHashMap()

    fun addDynamicParams(activity: Activity?, callback: IViewDynamicParamsProvider) {
        activity?.let {
            var list = dynamicParamsList[it]
            if (list == null) {
                list = mutableListOf()
                dynamicParamsList[it] = list
            }
            list.add(callback)
        }
    }

    private fun getCurrentMenuItemMap(activity: Activity?): MutableMap<Int, DataEntity>? {
        activity?.let {
            var map = menuItemInfo[it]
            if (map == null) {
                map = mutableMapOf()
                menuItemInfo[it] = map
            }
            return map
        }
        return null
    }

    private fun getCurrentMenuPageMap(activity: Activity?): MutableMap<Int, DataEntity>? {
        activity?.let {
            var map = menuPageInfo[it]
            if (map == null) {
                map = mutableMapOf()
                menuPageInfo[it] = map
            }
            return map
        }
        return null
    }

    fun setMenuItemDataEntity(activity: Activity?, menuId: Int, dataEntity: DataEntity) {
        getCurrentMenuItemMap(activity)?.put(menuId, dataEntity)
    }

    fun getMenuItemDataEntity(activity: Activity?, menuId: Int): DataEntity? {
        return getCurrentMenuItemMap(activity)?.get(menuId)
    }

    fun setMenuPageDataEntity(activity: Activity?, menuId: Int, dataEntity: DataEntity) {
        getCurrentMenuPageMap(activity)?.put(menuId, dataEntity)
    }

    fun getMenuPageDataEntity(activity: Activity?, menuId: Int): DataEntity? {
        return getCurrentMenuPageMap(activity)?.get(menuId)
    }

    private fun getActivity(context: Context?): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

    @SuppressLint("RestrictedApi")
    fun onMenuItemInitialize(itemView: ItemView?, itemData: MenuItemImpl?) {
        itemData?.itemId?.let {
            if (itemView is View) {
                val info = getCurrentMenuItemMap(getActivity(itemView.context))?.get(it)
                if (info != null && itemView.getTag(R.id.key_data_package) != info) {
                    itemView.setTag(R.id.key_data_package, info)
                    DataReportInner.getInstance().reBuildVTree(itemView)
                }
            }
        }

        callObserverAction { it.onMenuItemInitialize(itemView, itemData) }
    }

    @SuppressLint("RestrictedApi")
    fun onMenuPopupShow(popupWindow: MenuPopupWindow?, mMenu: MenuBuilder?) {
        val view = popupWindow?.listView
        if (mMenu is SubMenuBuilder) {
            if (view != null) {
                val info = getCurrentMenuPageMap(getActivity(view.context))?.get(mMenu.item.itemId)
                if (info != null && view.getTag(R.id.key_data_package) != info) {
                    view.setTag(R.id.key_data_package, info)
                }
            }
        }
        if (view != null) {
            DataReportInner.getInstance().setViewAsAlert(view, true, 1)
        }

        callObserverAction { it.onMenuPopupShow(popupWindow, mMenu) }
    }

    fun onMenuPopupDismiss(popupWindow: MenuPopupWindow?, mMenu: MenuBuilder?) {
        val view = popupWindow?.listView
        if (view != null) {
            DataReportInner.getInstance().setViewAsAlert(view, false, 1)
        }

        callObserverAction { it.onMenuPopupDismiss(popupWindow, mMenu) }
    }

    override fun onActivityCreate(activity: Activity?) {
        callObserverAction { it.onActivityCreate(activity) }
        super.onActivityCreate(activity)
    }

    override fun onActivityDestroyed(activity: Activity?) {
        super.onActivityDestroyed(activity)
        activity?.let {
            menuItemInfo.remove(it)
            menuPageInfo.remove(it)
            dynamicParamsList.remove(it)
        }
        callObserverAction { it.onActivityDestroy(activity) }
    }

    private fun callObserverAction(block: (target: LogicMenuObserver) -> Unit) {
        logicMenuObserverList.forEach {
            block.invoke(it)
        }
    }

}

interface LogicMenuObserver {
    fun onActivityCreate(activity: Activity?)
    fun onActivityDestroy(activity: Activity?)

    fun onMenuItemInitialize(itemView: ItemView?, itemData: MenuItemImpl?)

    fun onMenuPopupShow(popupWindow: MenuPopupWindow?, mMenu: MenuBuilder?)

    fun onMenuPopupDismiss(popupWindow: MenuPopupWindow?, mMenu: MenuBuilder?)
}