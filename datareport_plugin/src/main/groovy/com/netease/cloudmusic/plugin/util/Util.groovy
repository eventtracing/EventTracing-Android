package com.netease.cloudmusic.plugin.util

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import jdk.internal.org.objectweb.asm.ClassReader
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

public class Util {

    public static HashSet<String> targetPackages = []
    public static HashSet<String> excludePackages = []

    public static URLClassLoader urlClassLoader
    private static def urlList = []

    def static getClassPaths(Project project, Collection<TransformInput> inputs, Set<String> includeJars, Map<String, String> map) {

        initConfigParams(project)

        getAndroidJarPath(project)

        // 原始项目中引用的 classpathList
        getProjectClassPath(inputs)

        def urlArray = urlList as URL[]
        urlClassLoader = new URLClassLoader(urlArray)

        newSection()
    }

    /** 获取原始项目中的 ClassPath */
    def private static getProjectClassPath(Collection<TransformInput> inputs) {

        def classPath = []
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                classPath << dirInput.file.absolutePath
                urlList << dirInput.getFile().toURI().toURL()
            }
            input.jarInputs.each { JarInput jarInput ->
                classPath << jarInput.file.absolutePath
                urlList << jarInput.getFile().toURI().toURL()
            }
        }
        return classPath
    }

    /**
     * 编译环境中 android.jar 的路径
     */
    def static getAndroidJarPath(Project project) {
        String path = project.android.bootClasspath[0].toString()
        urlList << (new File(path)).toURI().toURL()
        return path
    }


    def static newSection() {
        StringBuilder builder = new StringBuilder()
        50.times { builder.append('--') }
        Log.d { builder.toString() }
    }

    def static initConfigParams(Project project) {

        HashSet<String> inputPackages = project.dataReportConfig.targetPackages
        if (inputPackages != null) {
            targetPackages.addAll(inputPackages);
            Log.i( "==============@targetPackages = ${targetPackages}==============")
        }

        // packages that want our plugin to exclude.
        HashSet<String> inputExcludePackages = project.dataReportConfig.excludePackages
        if (inputExcludePackages != null) {
            excludePackages.addAll(inputExcludePackages)
            Log.i("==============@excludePackages = ${excludePackages}==============")
        }

    }

    public static String getJarOutputName(JarInput jarInput) {
        String destName = jarInput.name
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith('.jar')) {
            destName = destName.substring(0, destName.length() - 4)
        }
        destName + '_' + hexName
    }

    public static void printStackTrace(Throwable throwable) {
        Log.d { "threadId = ${Thread.currentThread().id}, ${getStackTraceString(throwable)}" }
    }

    public static String getStackTraceString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter()
        PrintWriter printWriter = new PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        printWriter.flush()
        return stringWriter.toString()
    }

    /**
     * 该jar文件是否包含需要修改的类
     * @param jarFile
     * @return
     */
    public static boolean isJarNeedModify(File jarFile) {
        boolean modified = false;
        if (targetPackages != null && targetPackages.size() > 0) {
            if (jarFile) {
                /**
                 * 读取原jar
                 */
                def file = new JarFile(jarFile);
                Enumeration enumeration = file.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                    String entryName = jarEntry.getName();
                    String className
                    if (entryName.endsWith(".class")) {
                        if (shouldModifyClass(entryName)) {
                            modified = true;
                            break;
                        }
                    }
                }
                file.close();
            }
        }
        return modified;
    }

    /**
     * 只扫描特定包下的类
     * @param className 形如 android.app.Fragment 的类名
     * @return
     */
    public static boolean shouldModifyClass(String className) {
        // 先判断是否在非扫描包下
        if (excludePackages != null) {
            Iterator<String> iterator = excludePackages.iterator()
            // 注意，闭包里的return语句相当于continue，不会跳出遍历，故用while或for
            while (iterator.hasNext()) {
                String packagename = iterator.next()
                if (className.contains(packagename)) {
                    return false
                }
            }
        }
        if (targetPackages != null) {
            Iterator<String> iterator = targetPackages.iterator()
            // 注意，闭包里的return语句相当于continue，不会跳出遍历，故用while或for
            while (iterator.hasNext()) {
                String packagename = iterator.next()
                if (className.contains(packagename)) {
                    return needVisit(className)
                }
            }
        }
        return false
    }

    public static final String KEY_R_FILE = File.separator + 'R$'
    public static final String KEY_R_FILE_END = File.separator + 'R.class'
    public static final String KEY_LIB_FILE = 'com/netease/cloudmusic/datareport'

    private static boolean needVisit(String filePath) {
        if (filePath.contains(KEY_R_FILE) || filePath.endsWith(KEY_R_FILE_END)) {
            return false
        }
        if (filePath.contains(KEY_LIB_FILE)) {
            return false
        }
        return true
    }

    public static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) != 0
    }

    public static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) != 0
    }

    public static int convertOpcodes(int code) {
        int result = code
        switch (code) {
            case Opcodes.ILOAD:
                result = Opcodes.ISTORE
                break
            case Opcodes.ALOAD:
                result = Opcodes.ASTORE
                break
            case Opcodes.LLOAD:
                result = Opcodes.LSTORE
                break
            case Opcodes.FLOAD:
                result = Opcodes.FSTORE
                break
            case Opcodes.DLOAD:
                result = Opcodes.DSTORE
                break
            case Opcodes.ISTORE:
                result = Opcodes.ILOAD
                break
            case Opcodes.ASTORE:
                result = Opcodes.ALOAD
                break
            case Opcodes.LSTORE:
                result = Opcodes.LLOAD
                break
            case Opcodes.FSTORE:
                result = Opcodes.FLOAD
                break
            case Opcodes.DSTORE:
                result = Opcodes.DLOAD
                break
        }
        return result
    }

    static boolean subTypeOf(String className, String superName) {
        ClassReader reader = new ClassReader(urlClassLoader.getResourceAsStream(className.replace(".", "/") + ".class"))
        if (reader.superName == "java/lang/Object") {
            return false
        } else if (reader.superName == superName) {
            return true
        } else {
            return subTypeOf(reader.superName, superName)
        }
    }

    static boolean subTypeOfInterface(String className, String interfaceName) {
        try {
            ClassReader reader = new ClassReader(urlClassLoader.getResourceAsStream(className.replace(".", "/") + ".class"))
            String[] interfaces = reader.interfaces
            if (interfaces.contains(interfaceName)) {
                return true
            }
            if (interfaces.length > 0) {
                for (interf in interfaces) {
                    if (subTypeOfInterface(interf, interfaceName)) {
                        return true
                    }
                }
            }

            if (reader.superName == "java/lang/Object") {
                return false
            } else {
                return subTypeOfInterface(reader.superName, interfaceName)
            }
        } catch (Exception e) {
            e.printStackTrace()
            Log.i("---------------->error : className : ${className}  interfaceName : ${interfaceName}")
            throw e
        }
    }
}
