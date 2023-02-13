package com.netease.cloudmusic.plugin.processor

import com.android.build.api.transform.*
import com.netease.cloudmusic.plugin.util.Log
import com.netease.cloudmusic.plugin.util.Util
import org.apache.commons.io.FileUtils

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 增量AOP处理器
 * 1.只有Debug的二次增量编译时才会生效。
 * 2.为了保证安全，Release包固定的都是走全量AOP处理器的。
 * 3.和全量AOP处理器的统一做JAR压缩生成，统一做JAR文件和文件夹Copy不同。
 * 4.增量AOP处理器是单文件处理和单文件Copy的。和全量AOP的处理流程差别比较大。
 */
public class IncrementProcessor extends BaseProcessor {

    public IncrementProcessor(Context context){
        super(context)
    }

    /**
     * 执行注入操作
     */
    protected void doInject(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        long injectStartTime = System.currentTimeMillis()
        Util.newSection()
        doInjectImp(inputs, outputProvider)
        long handleInputTime = System.currentTimeMillis()
        Log.i(">>> DataReport, handleInputTime = ${handleInputTime - injectStartTime}")
    }

    protected doInjectImp(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        try {
            inputs.each { TransformInput input ->
                handleDirectoryInputs(input, outputProvider)
                handleJarInputs(input, outputProvider)
            }
        } catch (Throwable t) {
            Util.printStackTrace(t)
        }
    }

    private ArrayList<DirectoryInput> handleDirectoryInputs(TransformInput input, TransformOutputProvider outputProvider) {
        input.directoryInputs.each { DirectoryInput directoryInput ->
            File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY)
            FileUtils.forceMkdir(dest)
            String srcDirPath = directoryInput.getFile().getAbsolutePath()
            String destDirPath = dest.getAbsolutePath()
            ThreadPoolExecutor poolExecutor = Executors.newFixedThreadPool(8)
            Log.d { ">>> threadId = ${Thread.currentThread().id}, curPath = ${srcDirPath}, changeFiles = ${directoryInput.changedFiles}" }

            directoryInput.changedFiles.entrySet().each { Map.Entry<File, Status> changedFile ->
                Status status = changedFile.getValue()
                File inputFile = changedFile.getKey()
                poolExecutor.execute(new Runnable() {
                    @Override
                    void run() {
                        Log.d { ">>> threadId = ${Thread.currentThread().id}, inputFile = ${inputFile.absolutePath}, status = ${status}" }
                        String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath)
                        File destFile = new File(destFilePath)
                        switch (status) {
                            case Status.NOTCHANGED:
                                break
                            case Status.REMOVED:
                                Log.d { ">>> threadId = ${Thread.currentThread().id}, removedFile, filePath = ${inputFile} , status = ${status} , srcDirPath = ${srcDirPath}" }
                                if (destFile.exists()) {
                                    FileUtils.forceDelete(destFile)
                                }
                                break
                            case Status.ADDED:
                            case Status.CHANGED:
                                FileUtils.touch(destFile)
                                if (Util.shouldModifyClass(inputFile.absolutePath)) {
                                    startInjectSingleFile(inputFile, srcDirPath)
                                }
                                FileUtils.copyFile(inputFile, destFile)
                                Log.d { ">>> threadId = ${Thread.currentThread().id}, changedFile, filePath = ${inputFile} , status = ${status} , destFile = ${destFile}" }
                                break
                        }
                    }
                })
            };
            poolExecutor.shutdown();
            try {
                poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
            } catch (InterruptedException ex) {
            }
        }
    }

    private ArrayList<JarInput> handleJarInputs(TransformInput input, TransformOutputProvider outputProvider) {
        input.jarInputs.each { JarInput jarInput ->
            switch (jarInput.status) {
                case Status.NOTCHANGED:
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    Log.d { ">>> threadId = ${Thread.currentThread().id}, changedJar, filePath = ${jarInput.getFile().absolutePath} , status = ${jarInput.status}" }
                    handleJar(jarInput, outputProvider)
                    break
                case Status.REMOVED:
                    File dest = outputProvider.getContentLocation(Util.getJarOutputName(jarInput), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR)
                    Log.d { ">>> threadId = ${Thread.currentThread().id}, removedJar, filePath = ${jarInput.getFile().absolutePath} , status = ${jarInput.status}, dest = ${dest}, destExist = ${dest.exists()}" }
                    if (dest.exists()) {
                        FileUtils.forceDelete(dest)
                    }
                    break
            }
        }
    }

}
