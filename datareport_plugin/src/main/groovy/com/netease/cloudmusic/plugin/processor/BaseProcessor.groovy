package com.netease.cloudmusic.plugin.processor

import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.scope.GlobalScope
import com.netease.cloudmusic.plugin.asm.InjectorsClassVisitor
import com.netease.cloudmusic.plugin.util.Log
import com.netease.cloudmusic.plugin.util.Util
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import groovy.io.FileType
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public abstract class BaseProcessor {

    protected Context mContext
    public BaseProcessor(Context context){
        this.mContext = context
    }

    /* 需要处理的 jar 包 */
    def includeJars = [] as Set
    /* KEY为 jar 路径，VALUE为转存的临时 jar 路径*/
    def jarMap = [:]
    private int mClassCount = 0
    private Project mProject;

    /**
     * 执行 Transform
     */
    def doTransform(Project project, GlobalScope globalScope, Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        mProject = project

        long transStartTime = System.currentTimeMillis()
        initClassLoader(project, inputs)
        long initClassPoolTime = System.currentTimeMillis()

        doInject(inputs, outputProvider)
        long doInjectTime = System.currentTimeMillis()

        Log.i(">>> DataReport, initClassPoolTime = ${initClassPoolTime - transStartTime}, doInjectTime = ${doInjectTime - initClassPoolTime}, classCount = ${mClassCount}")
    }

    protected abstract void doInject(Collection<TransformInput> inputs, TransformOutputProvider outputProvider)

    /**
     * 初始化 ClassPool
     */
    def initClassLoader(Project project, Collection<TransformInput> inputs) {
        Util.newSection()
        // 添加编译时需要引用的到类到 ClassLoader
        Util.getClassPaths(project, inputs, includeJars, jarMap)
    }

    def startInjectClass(File dirFile) {
        Util.newSection()
        Log.d { ">>> threadId = ${Thread.currentThread().id}, startInjectDir: ${dirFile}" }
        ThreadPoolExecutor poolExecutor = Executors.newFixedThreadPool(8)
        if (!dirFile.exists()) {
            return
        }
        dirFile.traverse (type: FileType.FILES, nameFilter: ~/.*\.class/) { File classFile ->
            if (Util.shouldModifyClass(classFile.absolutePath)) {
                poolExecutor.execute(new Runnable() {
                    @Override
                    void run() {
                        startInjectSingleFile(classFile, dirFile.absolutePath)
                    }
                })
            }
        }
        poolExecutor.shutdown()
        try {
            poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
        } catch (InterruptedException ex) {
        }
    }

    protected void startInjectSingleFile(File file, String dir) {
        mClassCount++
        def stream
        try {
            stream = new FileInputStream(file)
            byte[] bytes = modifyClass(stream)
            outPutDebugDir(file, dir, bytes)
            OutputStream trueOut = new FileOutputStream(file)
            trueOut.write(bytes)
            trueOut.close()
        } catch (Throwable t) {
            Log.d { "    threadId = ${Thread.currentThread().id}, filePath = ${file.absolutePath}, [Warning] --> ${t.toString()}" }
            Util.printStackTrace(t)
        } finally {
            if (stream != null) {
                stream.close()
            }
        }
    }

    /**
     * 把插桩的类在 对应的文件夹下面生成
     * @param file
     * @param dir
     * @param bytes
     */
    private void outPutDebugDir(File file, String dir, byte[] bytes) {
        if (Log.isDebug()) {
            File tempFile = getDebugFile()
            File modified = new File(file.absolutePath.replace(dir, tempFile.getAbsolutePath()))
            outDebugFileInner(modified, bytes)
        }
    }

    private File getDebugFile() {
        String temp = mProject.getBuildDir().path + File.separator + "datareport"
        File tempFile = new File(temp)
        if (!tempFile.exists()) {
            tempFile.mkdirs()
        }
        return tempFile
    }

    private void outDebugFileInner(File modified, byte[] bytes){
        File parentFile = modified.getParentFile()
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        modified.createNewFile()
        OutputStream out = new FileOutputStream(modified)
        out.write(bytes)
        out.close()
    }

    private void outDebugFile(String entryName, byte[] bytes) {
        if (Log.isDebug()) {
            File tempFile = getDebugFile()
            File modified = new File(tempFile, entryName)
            outDebugFileInner(modified, bytes)
        }
    }

//=================================处理jar文件===============================
    def handleJar(JarInput input, TransformOutputProvider outputProvider) {
        String destName = input.name
        def hexName = DigestUtils.md5Hex(input.file.absolutePath)
        if (destName.endsWith('.jar')) {
            destName = destName.substring(0, destName.length() - 4)
        }

        /** 获得输出文件*/
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, input.contentTypes, input.scopes, Format.JAR);
        def modifiedJar = null;
        if (Util.isJarNeedModify(input.file)) {
            modifiedJar = modifyJarFile(input)
        }
        if (modifiedJar == null) {
            modifiedJar = input.file;
        }
        FileUtils.copyFile(modifiedJar, dest)
    }

    public File modifyJarFile(JarInput input) {
        File tempDir = mContext.getTemporaryDir();
        File jarFile = input.file
        if (jarFile) {
            /** 设置输出到的jar */
            def hexNameOpt = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
            def optJar = new File(tempDir, hexNameOpt + jarFile.name)
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

            /**
             * 读取原jar
             */
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();

                ZipEntry zipEntry = new ZipEntry(entryName);

                jarOutputStream.putNextEntry(zipEntry);

                byte[] modifiedClassBytes = null;
                if (entryName.endsWith(".class")) {
                    if (Util.shouldModifyClass(entryName)) {
                        modifiedClassBytes = modifyJarInputStream(file.getInputStream(jarEntry), entryName)
                    }
                }
                if (modifiedClassBytes == null) {
                    jarOutputStream.write(IOUtils.toByteArray(file.getInputStream(jarEntry)))
                } else {
                    jarOutputStream.write(modifiedClassBytes)
                }

                jarOutputStream.closeEntry()
            }
            jarOutputStream.close();
            file.close();
            return optJar
        } else {
            return null
        }
    }

    private byte[] modifyClass(InputStream stream) {
        try {
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
            ClassVisitor classVisitor = new InjectorsClassVisitor(classWriter)
            ClassReader cr = new ClassReader(stream)
            cr.accept(classVisitor, ClassReader.EXPAND_FRAMES + ClassReader.SKIP_FRAMES)
            return classWriter.toByteArray()
        } catch (Exception ex) {
            ex.printStackTrace()
            return srcClass
        }
    }

    byte[] modifyJarInputStream(InputStream stream, String entryName) {
        mClassCount++
        try {
            byte[] bytes = modifyClass(stream)
            outDebugFile(entryName, bytes)
            return bytes
        } catch (Throwable t) {
            Log.d { "    threadId = ${Thread.currentThread().id}, filePath = ${entryName}, [Warning] --> ${t.toString()}" }
            Util.printStackTrace(t)
            return null
        } finally {
            if (stream != null) {
                stream.close()
            }
        }
    }

}
