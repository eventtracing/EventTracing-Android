package com.netease.cloudmusic.plugin.processor

import com.android.build.api.transform.*
import com.netease.cloudmusic.plugin.util.Log
import com.netease.cloudmusic.plugin.util.Util
import org.apache.commons.io.FileUtils

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

public class FullProcessor extends BaseProcessor {

    public FullProcessor(Context context){
        super(context)
    }

    /**
     * 执行注入操作
     */
    protected void doInject(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        long injectStartTime = System.currentTimeMillis()
        Util.newSection()

        handleInputs(inputs, outputProvider)
        long handleInputTime = System.currentTimeMillis()
        Util.newSection()
        Log.i(">>> DataReport, handleInputTime = ${handleInputTime - injectStartTime}")
    }

    private void handleInputs(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        try {
            ThreadPoolExecutor poolExecutor = Executors.newFixedThreadPool(8)
            inputs.each { TransformInput input ->
                input.directoryInputs.each {
                    poolExecutor.execute(new Runnable() {
                        @Override
                        void run() {
                            handleDir(it, outputProvider)
                        }
                    })
                }
                input.jarInputs.each {
                    poolExecutor.execute(new Runnable() {
                        @Override
                        void run() {
                            handleJar(it, outputProvider)
                        }
                    })
                }
            }
            poolExecutor.shutdown()
            try {
                poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
            } catch (InterruptedException ex) {
            }
        } catch (Throwable t) {
            Util.printStackTrace(t)
        }
    }

    /**
     * 处理目录中的 class 文件
     */
    def handleDir(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        Log.d { ">>> threadId = ${Thread.currentThread().id}, Handle Dir: ${directoryInput.file.absolutePath}" }
        startInjectClass(directoryInput.file)
        File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

}
