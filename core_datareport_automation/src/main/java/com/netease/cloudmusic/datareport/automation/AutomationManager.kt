package com.netease.cloudmusic.datareport.automation

import android.app.Activity
import android.view.View

object AutomationManager {

    fun setAutomationOid(view: View, oid: String) {
        AutomationDataRWProxy.with(view)?.setOid(oid)
    }

    fun setAutomationPos(view: View, pos: Int) {
        AutomationDataRWProxy.with(view)?.setPos(pos)
    }

    fun setAutomationMenuItem(activity: Activity, menuId: Int, oid: String, pos: Int?) {
        AutomationLogicMenuManager.setMenuItemDataEntity(activity, menuId, AutomationEntity(oid, pos))
    }

    fun setAutomationMenuPage(activity: Activity, menuId: Int, oid: String, pos: Int?) {
        AutomationLogicMenuManager.setMenuPageDataEntity(activity, menuId, AutomationEntity(oid, pos))
    }

    fun getSpm(view: View): String? {
        return AutomationDataRWProxy.with(view)?.getSpm()
    }
}