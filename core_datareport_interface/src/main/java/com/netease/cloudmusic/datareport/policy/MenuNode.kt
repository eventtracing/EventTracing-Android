package com.netease.cloudmusic.datareport.policy

import android.app.Activity
import androidx.annotation.IntDef
import com.netease.cloudmusic.datareport.policy.MenuNode.Companion.MENU_ITEM_RES
import com.netease.cloudmusic.datareport.policy.MenuNode.Companion.MENU_PAGE_RES
import java.lang.ref.WeakReference

/**
 * 主要用来对系统菜单进行埋点的bean
 * @param activity 当前菜单对应的activity
 * @param menuRes 当前菜单对应的资源ID
 * @param type 菜单类型
 */
class MenuNode(
        activity: Activity,
        val menuRes: Int,
        @MenuNodeType val type: Int) {

    companion object {
        const val MENU_ITEM_RES = 1
        const val MENU_PAGE_RES = 2
    }
    private val mActivity = WeakReference<Activity>(activity)

    fun getActivity(): Activity? {
        return mActivity.get()
    }

}

@IntDef(MENU_ITEM_RES, MENU_PAGE_RES)
@Retention(AnnotationRetention.SOURCE)
annotation class MenuNodeType