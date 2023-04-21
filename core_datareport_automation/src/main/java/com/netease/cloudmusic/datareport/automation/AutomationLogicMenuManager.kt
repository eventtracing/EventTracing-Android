package com.netease.cloudmusic.datareport.automation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.view.menu.MenuView
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.MenuPopupWindow
import com.netease.cloudmusic.datareport.vtree.logic.LogicMenuManager
import com.netease.cloudmusic.datareport.vtree.logic.LogicMenuObserver
import java.util.*

object AutomationLogicMenuManager : LogicMenuObserver {

    init {
        LogicMenuManager.addObserver(this)
    }


    private var menuItemInfo: WeakHashMap<Activity, MutableMap<Int, AutomationEntity>> = WeakHashMap()
    private var menuPageInfo: WeakHashMap<Activity, MutableMap<Int, AutomationEntity>> = WeakHashMap()


    private fun getCurrentMenuItemMap(activity: Activity?): MutableMap<Int, AutomationEntity>? {
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

    private fun getCurrentMenuPageMap(activity: Activity?): MutableMap<Int, AutomationEntity>? {
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

    fun setMenuItemDataEntity(activity: Activity?, menuId: Int, dataEntity: AutomationEntity) {
        getCurrentMenuItemMap(activity)?.put(menuId, dataEntity)
    }

    fun setMenuPageDataEntity(activity: Activity?, menuId: Int, dataEntity: AutomationEntity) {
        getCurrentMenuPageMap(activity)?.put(menuId, dataEntity)
    }

    override fun onMenuItemInitialize(itemView: MenuView.ItemView?, itemData: MenuItemImpl?) {
        itemData?.itemId?.let {
            if (itemView is View) {
                val info = getCurrentMenuItemMap(getActivity(itemView.context))?.get(it)
                if (info != null) {
                    val tempEntity = AutomationDataRWProxy.getDataEntity(itemView, true)
                    tempEntity?.oid = info.oid
                    tempEntity?.pos = info.pos
                    tempEntity?.spm = null
                }
            }
        }
    }

    override fun onMenuPopupShow(popupWindow: MenuPopupWindow?, mMenu: MenuBuilder?) {
        val view = popupWindow?.listView
        if (mMenu is SubMenuBuilder) {
            if (view != null) {
                val info = getCurrentMenuPageMap(getActivity(view.context))?.get(mMenu.item.itemId)
                if (info != null) {
                    val tempEntity = AutomationDataRWProxy.getDataEntity(view, true)
                    tempEntity?.oid = info.oid
                    tempEntity?.pos = info.pos
                    tempEntity?.spm = null
                }
            }
        }
    }

    private fun getActivity(context: Context?): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

    override fun onMenuPopupDismiss(popupWindow: MenuPopupWindow?, mMenu: MenuBuilder?) {
    }

    override fun onActivityCreate(activity: Activity?) {
    }

    override fun onActivityDestroy(activity: Activity?) {
        activity?.let {
            menuItemInfo.remove(it)
            menuPageInfo.remove(it)
        }
    }

}