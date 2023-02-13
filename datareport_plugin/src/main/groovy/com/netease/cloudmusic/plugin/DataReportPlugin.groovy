package com.netease.cloudmusic.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.netease.cloudmusic.plugin.util.Log
import org.gradle.api.Plugin
import org.gradle.api.Project

public class DataReportPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.extensions.create('dataReportConfig', DataReportParams)

        Log.i("start datareport plugin ! ")

        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            def android = project.extensions.getByType(AppExtension)

            android.registerTransform(new DataReportTransform(project))  // 将 transform 注册到 android
        }
    }
}