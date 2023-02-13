package com.netease.cloudmusic.plugin.asm

import kotlin.Pair
import org.objectweb.asm.Opcodes

class LambdaInsertRule {

    public final static HashMap<String, Pair<MethodInfo, MethodInfo>> LAMBDA_METHODS = new HashMap<>()

    static {
        addLambdaMethod(new MethodInfo(
                'onClick',
                '(Landroid/view/View;)V',
                'Landroid/view/View$OnClickListener;',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]),
                new MethodInfo(
                        'onClick',
                        '(Landroid/view/View;)V',
                        'Landroid/view/View$OnClickListener;',
                        'onViewPreClickedStatic',
                        '(Landroid/view/View;)V',
                        1, 1,
                        [Opcodes.ALOAD]))

        addLambdaMethod(new MethodInfo(
                'onItemClick',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                'Landroid/widget/AdapterView$OnItemClickListener;',
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;I)V',
                1, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD]),
                new MethodInfo(
                        'onItemClick',
                        '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                        'Landroid/widget/AdapterView$OnItemClickListener;',
                        'onViewPreClickedStatic',
                        '(Landroid/view/View;)V',
                        1, 1,
                        [Opcodes.ALOAD]))

        addLambdaMethod(new MethodInfo(
                'onCheckedChanged',
                '(Landroid/widget/RadioGroup;I)V',
                'Landroid/widget/RadioGroup$OnCheckedChangeListener;',
                'trackRadioGroup',
                '(Landroid/widget/RadioGroup;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]),
                new MethodInfo(
                        'onCheckedChanged',
                        '(Landroid/widget/RadioGroup;I)V',
                        'Landroid/widget/RadioGroup$OnCheckedChangeListener;',
                        'onViewPreClickedStatic',
                        '(Landroid/view/View;)V',
                        1, 1,
                        [Opcodes.ALOAD]))

        addLambdaMethod(new MethodInfo(
                'onCheckedChanged',
                '(Landroid/widget/CompoundButton;Z)V',
                'Landroid/widget/CompoundButton$OnCheckedChangeListener;',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]),
                new MethodInfo(
                        'onCheckedChanged',
                        '(Landroid/widget/CompoundButton;Z)V',
                        'Landroid/widget/CompoundButton$OnCheckedChangeListener;',
                        'onViewPreClickedStatic',
                        '(Landroid/view/View;)V',
                        1, 1,
                        [Opcodes.ALOAD]))
    }

    static void addLambdaMethod(MethodInfo methodInfo, MethodInfo beforeMethodInfo) {
        if (methodInfo != null && beforeMethodInfo != null) {
            LAMBDA_METHODS.put(methodInfo.parent + methodInfo.name + methodInfo.desc, new Pair<MethodInfo, MethodInfo>(methodInfo, beforeMethodInfo))
        }
    }
}