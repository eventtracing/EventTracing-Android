package com.netease.datareport.demo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.policy.MenuNode

class DemoMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        NodeBuilder.setPageId(this, "DemoMainActivity")

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    /**
     * 给菜单进行埋点
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_report, menu)

        menu?.add(0, 1, 1, "aaaaa")?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        val subMenu = menu?.addSubMenu(0, 2, 1, "ffffff")
        subMenu?.getItem()?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        subMenu?.add(0, 3, 1, "bbbbb")
        val sub2 = subMenu?.addSubMenu(0, 4, 1, "cccccc")
        sub2?.add(0, 5, 1, "eeeee")
        sub2?.add(0, 6, 1, "ggggggg")

        DataReport.getInstance()
            .setElementId(MenuNode(this, R.id.option_normal_1, MenuNode.MENU_ITEM_RES), "action1")
            .setElementId(MenuNode(this, R.id.option_normal_2, MenuNode.MENU_ITEM_RES), "action2")
            .setElementId(MenuNode(this, 1, MenuNode.MENU_ITEM_RES), "aaaaa")
            .setElementId(MenuNode(this, 2, MenuNode.MENU_ITEM_RES), "ffffff")
            .setElementId(MenuNode(this, 3, MenuNode.MENU_ITEM_RES), "bbbbb")
            .setElementId(MenuNode(this, 4, MenuNode.MENU_ITEM_RES), "cccccc")
            .setElementId(MenuNode(this, 5, MenuNode.MENU_ITEM_RES), "eeeee")
            .setElementId(MenuNode(this, 6, MenuNode.MENU_ITEM_RES), "ggggggg")
            .setPageId(MenuNode(this, 2, MenuNode.MENU_PAGE_RES), "page_ffffff")
            .setPageId(MenuNode(this, 4, MenuNode.MENU_PAGE_RES), "page_cccccc")

        return true
    }
}