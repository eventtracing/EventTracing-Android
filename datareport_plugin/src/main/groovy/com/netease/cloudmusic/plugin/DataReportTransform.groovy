package com.netease.cloudmusic.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.netease.cloudmusic.plugin.processor.BaseProcessor
import com.netease.cloudmusic.plugin.processor.FullProcessor
import com.netease.cloudmusic.plugin.processor.IncrementProcessor
import com.netease.cloudmusic.plugin.util.Log
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.regex.Pattern

public class DataReportTransform extends Transform {

    private Project project
    private def globalScope
    private def appPlugin
    private boolean needIncremental = true

    public DataReportTransform(Project project) {
        this.project = project
        appPlugin = project.plugins.getPlugin(AppPlugin)
        globalScope = project.extensions.getByType(AppExtension).globalScope
    }

    @Override
    String getName() {
        return 'NeteaseDataReport'
    }

    @Override
    void transform(Context context,
                   Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {

        Log.setDebug(project.dataReportConfig.openDebugLog)

        long startTime = System.currentTimeMillis()
        welcome()
        needIncremental = isIncremental && "release" != context.getVariantName()
        if (!needIncremental) {
            outputProvider.deleteAll()
        }
        printVariantInfo(outputProvider, context, isIncremental, needIncremental)
        doTransform(context, inputs, outputProvider)
        long endTime = System.currentTimeMillis()
        Log.i(">>> DataReport, totalTime = ${endTime - startTime}")
    }

    private void printVariantInfo(TransformOutputProvider outputProvider, Context context, boolean isIncremental, boolean needIncremental) {
        File rootLocation
        try {
            rootLocation = outputProvider.rootLocation
        } catch (Throwable e) {
            rootLocation = outputProvider.folderUtils.getRootFolder()
        }
        if (rootLocation == null) {
            throw new GradleException("can't get transform root location")
        }
        Log.i(">>> rootLocation: ${rootLocation}， variantName = ${context.getVariantName()}, isIncrement = ${isIncremental}, needIncrement = ${needIncremental} ")
        // Compatible with path separators for window and Linux, and fit split param based on 'Pattern.quote'
        def variantDir = rootLocation.absolutePath.split(getName() + Pattern.quote(File.separator))[1]
        Log.i(">>> variantDir: ${variantDir}")
    }

    /**
     * 执行 Transform
     */
    def doTransform(Context context, Collection<TransformInput> inputs,
                    TransformOutputProvider outputProvider) {
        BaseProcessor processor
        if (needIncremental) {
            processor = new IncrementProcessor(context)
        } else {
            processor = new FullProcessor(context)
        }
        processor.doTransform(project, globalScope, inputs, outputProvider)
    }

    /**
     * 欢迎
     */
    static def welcome() {
        StringBuilder sb = new StringBuilder()
        60.times { sb.append('=') }
        String line = sb.append('\n').toString()
        Log.i('\n')
        Log.i(line)
        Log.i('                    datareport-plugin')
        Log.i(line)
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }
}
