package com.netease.cloudmusic.plugin.asm

import com.netease.cloudmusic.plugin.util.Util
import kotlin.Pair
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class InjectorsClassVisitor extends ClassVisitor {
    static final String EVENT_COLLECTOR = "com/netease/cloudmusic/datareport/inject/EventCollector"
    private String mClassName //class的名称
    private String mSuperName //class的父类名称
    private String[] mInterfaces //class的实现的接口的列表
    private ClassVisitor classVisitor

    private HashMap<String, Pair<MethodInfo, MethodInfo>> mLambdaMethodCells = new HashMap<>()

    InjectorsClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        this.classVisitor = classVisitor
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        mClassName = name
        mSuperName = superName
        mInterfaces = interfaces
        //如果父类的名称在列表里面，就替换掉
        String replaceSuper = ReplaceClassRule.PARENT_CLASS.get(superName)
        if (replaceSuper != null) {
            mSuperName = replaceSuper
        }
        super.visit(version, access, name, signature, mSuperName, interfaces)
    }

    private static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode, String owner, String methodName, String methodDesc, int start, int count, List<Integer> paramOpcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(paramOpcodes[i - start], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)

        AdviceAdapter adviceAdapter = new AdviceAdapter(Opcodes.ASM6, methodVisitor, access, name, descriptor) {

            //方法名称加方法签名
            String nameDesc
            boolean isHasTracked = false
            boolean isTargetMethod
            boolean isOnClickMethod = false
            int variableID = 0
            ArrayList<Integer> localIds
            boolean isOnItemClickMethod = false

            @Override
            void visitEnd() {
                super.visitEnd()
                if (isHasTracked) {
                    if (mLambdaMethodCells.containsKey(nameDesc)) {
                        mLambdaMethodCells.remove(nameDesc)
                    }
                }
            }

            @Override
            void visitInsn(int opcode) {
                //对LayoutInflater中返回的ScrollView进行替换
                if (nameDesc == "onCreateView(Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;" && opcode == ARETURN && mInterfaces.contains("android/view/LayoutInflater\$Factory")) {
                    super.visitInsn(ACONST_NULL)
                    super.visitVarInsn(ALOAD, 1)
                    super.visitVarInsn(ALOAD, 2)
                    super.visitVarInsn(ALOAD, 3)
                    super.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, "getScrollView", "(Landroid/view/View;Landroid/view/View;Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;", false);
                } else if (nameDesc == "onCreateView(Landroid/view/View;Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;" && opcode == ARETURN && mInterfaces.contains("android/view/LayoutInflater\$Factory2")) {
                    super.visitVarInsn(ALOAD, 1)
                    super.visitVarInsn(ALOAD, 2)
                    super.visitVarInsn(ALOAD, 3)
                    super.visitVarInsn(ALOAD, 4)
                    super.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, "getScrollView", "(Landroid/view/View;Landroid/view/View;Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;", false);
                } else if (nameDesc == "onCreateView(Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;" && opcode == ARETURN && mInterfaces.contains("android/view/LayoutInflater\$Factory2")) {
                    super.visitInsn(ACONST_NULL)
                    super.visitVarInsn(ALOAD, 1)
                    super.visitVarInsn(ALOAD, 2)
                    super.visitVarInsn(ALOAD, 3)
                    super.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, "getScrollView", "(Landroid/view/View;Landroid/view/View;Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;", false);
                }
                super.visitInsn(opcode)
            }

            @Override
            void visitTypeInsn(int opcode, String type) {
                //构建匿名内部类的时候，替换的构建的类 和 方法 visitMethodInsn 一起实现
                if (opcode == NEW && ReplaceClassRule.INNER_CLASS.containsKey(type)) {
                    super.visitTypeInsn(opcode, ReplaceClassRule.INNER_CLASS.get(type))
                } else {
                    super.visitTypeInsn(opcode, type)
                }
            }

            @Override
            void visitMethodInsn(int opcode, String owner, String n, String desc, boolean isInterface) {
                //构建匿名内部类的时候，替换的构建的类 和 方法 visitTypeInsn 一起实现
                if (opcode == INVOKESPECIAL && ReplaceClassRule.INNER_CLASS.containsKey(owner) && "<init>" == n) {
                    super.visitMethodInsn(opcode, ReplaceClassRule.INNER_CLASS.get(owner), n, desc, isInterface)
                } else {
                    super.visitMethodInsn(opcode, owner, n, desc, isInterface)
                }
            }

            @Override
            void visitInvokeDynamicInsn(String n, String desc, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                super.visitInvokeDynamicInsn(n, desc, bootstrapMethodHandle, bootstrapMethodArguments)
                try {
                    String desc2 = (String) bootstrapMethodArguments[0]
                    Pair<MethodInfo, MethodInfo> pair = LambdaInsertRule.LAMBDA_METHODS.get(Type.getReturnType(desc).getDescriptor() + n + desc2)
                    if (pair != null) {
                        Handle it = (Handle) bootstrapMethodArguments[1]
                        mLambdaMethodCells.put(it.name + it.desc, pair)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()
                nameDesc = name + descriptor
                isTargetMethod = Util.isPublic(access) && !Util.isStatic(access)
                if (isTargetMethod) {
                    insertMethodBefore(nameDesc, methodVisitor)
                }

                Pair<MethodInfo, MethodInfo> pair = mLambdaMethodCells.get(nameDesc)
                if (pair != null) {
                    MethodInfo info = pair.first
                    Type[] types = Type.getArgumentTypes(info.desc)
                    int length = types.length
                    Type[] lambdaTypes = Type.getArgumentTypes(descriptor)

                    int paramStart = lambdaTypes.length - length
                    if (paramStart < 0) {
                        return
                    } else {
                        for (int i = 0; i < length; i++) {
                            if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                                return
                            }
                        }
                    }
                    boolean isStaticMethod = Util.isStatic(access)
                    MethodInfo secondInfo = pair.second
                    for (int i = paramStart; i < paramStart + secondInfo.paramsCount; i++) {
                        methodVisitor.visitVarInsn(secondInfo.opcodes.get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod))
                    }
                    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, EVENT_COLLECTOR, secondInfo.agentName, secondInfo.agentDesc, false)
                    isHasTracked = true

                    localIds = new ArrayList<>()
                    for (int i = paramStart; i < paramStart + info.paramsCount; i++) {
                        int localId = newLocal(types[i - paramStart])
                        methodVisitor.visitVarInsn(info.opcodes.get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod))
                        methodVisitor.visitVarInsn(Util.convertOpcodes(info.opcodes.get(i - paramStart)), localId)
                        localIds.add(localId)
                    }
                }

                if (nameDesc == "dismiss()V" && mClassName == "androidx/appcompat/view/menu/StandardMenuPopup") {
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    methodVisitor.visitFieldInsn(GETFIELD, "androidx/appcompat/view/menu/StandardMenuPopup", "mPopup", "Landroidx/appcompat/widget/MenuPopupWindow;")
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    methodVisitor.visitFieldInsn(GETFIELD, "androidx/appcompat/view/menu/StandardMenuPopup", "mMenu", "Landroidx/appcompat/view/menu/MenuBuilder;")
                    methodVisitor.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, "onMenuPopupDismissStatic", "(Landroidx/appcompat/widget/MenuPopupWindow;Landroidx/appcompat/view/menu/MenuBuilder;)V", false);
                }
            }

            private void insertMethodBefore(String nameDesc, MethodVisitor visitor) {
                if ((nameDesc == 'onClick(Landroid/view/View;)V')) { //对点击方法进行插桩
                    isOnClickMethod = true
                    MethodInfo info = CodeInsertRule.BEFORE_METHODS.get(nameDesc)
                    if (info != null) { //在最前面插桩
                        visitMethodWithLoadedParams(visitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                        isHasTracked = true
                    }

                    variableID = newLocal(Type.getObjectType("java/lang/Integer"))
                    visitor.visitVarInsn(ALOAD, 1)
                    visitor.visitVarInsn(ASTORE, variableID)
                } else if (nameDesc == 'onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V') {
                    //对列表Item点击方法进行插桩
                    localIds = new ArrayList<>()
                    isOnItemClickMethod = true

                    MethodInfo info = CodeInsertRule.BEFORE_METHODS.get("android/widget/AdapterView\$OnItemClickListener" + nameDesc)
                    if (info != null) { //在最前面插桩
                        visitMethodWithLoadedParams(visitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                        isHasTracked = true
                    }

                    int first = newLocal(Type.getObjectType("android/widget/AdapterView"))
                    visitor.visitVarInsn(ALOAD, 1)
                    visitor.visitVarInsn(ASTORE, first)
                    localIds.add(first)

                    int second = newLocal(Type.getObjectType("android/view/View"))
                    visitor.visitVarInsn(ALOAD, 2)
                    visitor.visitVarInsn(ASTORE, second)
                    localIds.add(second)

                    int third = newLocal(Type.INT_TYPE)
                    visitor.visitVarInsn(ILOAD, 3)
                    visitor.visitVarInsn(ISTORE, third)
                    localIds.add(third)
                } else if (nameDesc == "onCheckedChanged(Landroid/widget/RadioGroup;I)V") {
                    MethodInfo info = CodeInsertRule.BEFORE_METHODS.get("android/widget/RadioGroup\$OnCheckedChangeListener" + nameDesc)
                    if (info != null) {
                        visitMethodWithLoadedParams(visitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                        isHasTracked = true
                    }
                    localIds = new ArrayList<>()
                    int firstLocalId = newLocal(Type.getObjectType("android/widget/RadioGroup"))
                    visitor.visitVarInsn(ALOAD, 1)
                    visitor.visitVarInsn(ASTORE, firstLocalId)
                    localIds.add(firstLocalId)
                    int secondLocalId = newLocal(Type.INT_TYPE)
                    visitor.visitVarInsn(ILOAD, 2)
                    visitor.visitVarInsn(ISTORE, secondLocalId)
                    localIds.add(secondLocalId)
                } else if (nameDesc == "onCheckedChanged(Landroid/widget/CompoundButton;Z)V") {
                    MethodInfo info = CodeInsertRule.BEFORE_METHODS.get("android/widget/CompoundButton\$OnCheckedChangeListener" + nameDesc)
                    if (info != null) {
                        visitMethodWithLoadedParams(visitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                        isHasTracked = true
                    }
                    localIds = new ArrayList<>()
                    int firstLocalId = newLocal(Type.getObjectType("android/widget/CompoundButton"))
                    visitor.visitVarInsn(ALOAD, 1)
                    visitor.visitVarInsn(ASTORE, firstLocalId)
                    localIds.add(firstLocalId)
                } else if (nameDesc == "onStopTrackingTouch(Landroid/widget/SeekBar;)V") {
                    MethodInfo info = CodeInsertRule.BEFORE_METHODS.get("android/widget/SeekBar\$OnSeekBarChangeListener" + nameDesc)
                    if (info != null) {
                        visitMethodWithLoadedParams(visitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                        isHasTracked = true
                    }
                    localIds = new ArrayList<>()
                    int firstLocalId = newLocal(Type.getObjectType("android/widget/SeekBar"))
                    visitor.visitVarInsn(ALOAD, 1)
                    visitor.visitVarInsn(ASTORE, firstLocalId)
                    localIds.add(firstLocalId)
                } else if (nameDesc == "onStopNestedScroll(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;I)V") {
                    localIds = new ArrayList<>()
                    int firstLocalId = newLocal(Type.getObjectType("android/widget/SeekBar"))
                    visitor.visitVarInsn(ALOAD, 1)
                    visitor.visitVarInsn(ASTORE, firstLocalId)
                    localIds.add(firstLocalId)
                }
            }

            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode)
                inertMethodLast()
            }

            void inertMethodLast() {
                Pair<MethodInfo, MethodInfo> pair = mLambdaMethodCells.get(nameDesc)
                if (pair != null) {
                    MethodInfo lambdaMethodCell = pair.first
                    Type[] types = Type.getArgumentTypes(lambdaMethodCell.desc)
                    int length = types.length
                    Type[] lambdaTypes = Type.getArgumentTypes(descriptor)

                    int paramStart = lambdaTypes.length - length
                    if (paramStart < 0) {
                        return
                    } else {
                        for (int i = 0; i < length; i++) {
                            if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                                return
                            }
                        }
                    }

                    for (int i = paramStart; i < paramStart + lambdaMethodCell.paramsCount; i++) {
                        methodVisitor.visitVarInsn(lambdaMethodCell.opcodes.get(i - paramStart), localIds[i - paramStart])
                    }
                    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, EVENT_COLLECTOR, lambdaMethodCell.agentName, lambdaMethodCell.agentDesc, false)
                    isHasTracked = true
                    return
                }
                if (isOnClickMethod && mClassName == 'android/databinding/generated/callback/OnClickListener') {
                    trackViewOnClick(methodVisitor, 1)
                    isHasTracked = true
                    return
                }

                if (mInterfaces != null && mInterfaces.length > 0) {
                    if (isOnItemClickMethod && mInterfaces.contains('android/widget/AdapterView$OnItemClickListener')) {
                        methodVisitor.visitVarInsn(ALOAD, localIds.get(0))
                        methodVisitor.visitVarInsn(ALOAD, localIds.get(1))
                        methodVisitor.visitVarInsn(ILOAD, localIds.get(2))
                        methodVisitor.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, "trackListView", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false)
                        isHasTracked = true
                        return
                    } else if (mInterfaces.contains('android/widget/RadioGroup$OnCheckedChangeListener')
                            && nameDesc == 'onCheckedChanged(Landroid/widget/RadioGroup;I)V') {
                        MethodInfo sensorsAnalyticsMethodCell = CodeInsertRule.INTERFACE_METHODS
                                .get('android/widget/RadioGroup$OnCheckedChangeListeneronCheckedChanged(Landroid/widget/RadioGroup;I)V')
                        if (sensorsAnalyticsMethodCell != null) {
                            methodVisitor.visitVarInsn(ALOAD, localIds.get(0))
                            methodVisitor.visitVarInsn(ILOAD, localIds.get(1))
                            methodVisitor.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, false)
                            isHasTracked = true
                            return
                        }
                    } else if (mInterfaces.contains('android/widget/CompoundButton$OnCheckedChangeListener')
                            && nameDesc == 'onCheckedChanged(Landroid/widget/CompoundButton;Z)V') {
                        MethodInfo sensorsAnalyticsMethodCell = CodeInsertRule.INTERFACE_METHODS
                                .get('android/widget/CompoundButton$OnCheckedChangeListeneronCheckedChanged(Landroid/widget/CompoundButton;Z)V')
                        if (sensorsAnalyticsMethodCell != null) {
                            methodVisitor.visitVarInsn(ALOAD, localIds.get(0))
                            methodVisitor.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, false)
                            isHasTracked = true
                            return
                        }
                    } else if (mInterfaces.contains('android/widget/SeekBar$OnSeekBarChangeListener')
                            && nameDesc == 'onStopTrackingTouch(Landroid/widget/SeekBar;)V') {
                        MethodInfo sensorsAnalyticsMethodCell = CodeInsertRule.INTERFACE_METHODS
                                .get('android/widget/SeekBar$OnSeekBarChangeListeneronStopTrackingTouch(Landroid/widget/SeekBar;)V')
                        if (sensorsAnalyticsMethodCell != null) {
                            methodVisitor.visitVarInsn(ALOAD, localIds.get(0))
                            methodVisitor.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, false)
                            isHasTracked = true
                            return
                        }
                    } else {
                        for (interfaceName in mInterfaces) {
                            MethodInfo sensorsAnalyticsMethodCell = CodeInsertRule.INTERFACE_METHODS.get(interfaceName + nameDesc)
                            if (sensorsAnalyticsMethodCell != null) {
                                visitMethodWithLoadedParams(methodVisitor, INVOKESTATIC, EVENT_COLLECTOR, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, sensorsAnalyticsMethodCell.paramsStart, sensorsAnalyticsMethodCell.paramsCount, sensorsAnalyticsMethodCell.opcodes)
                                isHasTracked = true
                                return
                            }
                        }
                    }
                }

                MethodInfo info = CodeInsertRule.SUPER_CLASS_METHODS.get(nameDesc)
                if (info != null && Util.subTypeOf(mClassName, info.parent)) {
                    visitMethodWithLoadedParams(methodVisitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                    isHasTracked = true
                    return
                }

                info = CodeInsertRule.CLASS_METHODS.get(mClassName + nameDesc)
                if (info != null) {
                    visitMethodWithLoadedParams(methodVisitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                    isHasTracked = true
                    return
                }

                info = CodeInsertRule.SUPER_INTERFACE_METHODS.get(nameDesc)
                if (info != null && Util.subTypeOfInterface(mClassName, info.parent)) {
                    visitMethodWithLoadedParams(methodVisitor, INVOKESTATIC, EVENT_COLLECTOR, info.agentName, info.agentDesc, info.paramsStart, info.paramsCount, info.opcodes)
                    isHasTracked = true
                    return
                }

                if (isOnClickMethod) {
                    trackViewOnClick(methodVisitor, variableID)
                    isHasTracked = true
                    return
                }

                if(nameDesc == "show()V" && mClassName == "androidx/appcompat/view/menu/StandardMenuPopup") {
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    methodVisitor.visitFieldInsn(GETFIELD, "androidx/appcompat/view/menu/StandardMenuPopup", "mPopup", "Landroidx/appcompat/widget/MenuPopupWindow;")
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    methodVisitor.visitFieldInsn(GETFIELD, "androidx/appcompat/view/menu/StandardMenuPopup", "mMenu", "Landroidx/appcompat/view/menu/MenuBuilder;")
                    methodVisitor.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, "onMenuPopupShowStatic", "(Landroidx/appcompat/widget/MenuPopupWindow;Landroidx/appcompat/view/menu/MenuBuilder;)V", false);
                }
            }

            void trackViewOnClick(MethodVisitor mv, int index) {
                mv.visitVarInsn(ALOAD, index)
                mv.visitMethodInsn(INVOKESTATIC, EVENT_COLLECTOR, "trackViewOnClick", "(Landroid/view/View;)V", false)
            }
        }

        return adviceAdapter
    }
    int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error")
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize()
        }
    }
}