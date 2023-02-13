package com.netease.cloudmusic.plugin.asm

import org.objectweb.asm.Opcodes

class CodeInsertRule {
    public final static HashMap<String, MethodInfo> INTERFACE_METHODS = new HashMap<>()
    public final static HashMap<String, MethodInfo> SUPER_INTERFACE_METHODS = new HashMap<>()
    public final static HashMap<String, MethodInfo> SUPER_CLASS_METHODS = new HashMap<>()
    public final static HashMap<String, MethodInfo> CLASS_METHODS = new HashMap<>()
    public final static HashMap<String, MethodInfo> BEFORE_METHODS = new HashMap<>()
    static {
        //在onClick的方法前面插桩
        addBeforeMethod(new MethodInfo(
                'onClick',
                '(Landroid/view/View;)V',
                '',
                'onViewPreClickedStatic',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        //在onItemClick的方法前面插桩
        addBeforeMethod(new MethodInfo(
                'onItemClick',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                'android/widget/AdapterView$OnItemClickListener',
                'onViewPreClickedStatic',
                '(Landroid/view/View;)V',
                2, 1,
                [Opcodes.ALOAD]))
        //在onCheckedChanged的方法前面插桩
        addBeforeMethod(new MethodInfo(
                'onCheckedChanged',
                '(Landroid/widget/RadioGroup;I)V',
                'android/widget/RadioGroup$OnCheckedChangeListener',
                'onViewPreClickedStatic',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        //在onCheckedChanged的方法前面插桩
        addBeforeMethod(new MethodInfo(
                'onCheckedChanged',
                '(Landroid/widget/CompoundButton;Z)V',
                'android/widget/CompoundButton$OnCheckedChangeListener',
                'onViewPreClickedStatic',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        //在onStopTrackingTouch的方法前面插桩
        addBeforeMethod(new MethodInfo(
                'onStopTrackingTouch',
                '(Landroid/widget/SeekBar;)V',
                'android/widget/SeekBar$OnSeekBarChangeListener',
                'onViewPreClickedStatic',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        //在onCheckedChanged的方法最后插桩
        addInterfaceMethod(new MethodInfo(
                'onCheckedChanged',
                '(Landroid/widget/CompoundButton;Z)V',
                'android/widget/CompoundButton$OnCheckedChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        //在onCheckedChanged的方法最后插桩
        addInterfaceMethod(new MethodInfo(
                'onCheckedChanged',
                '(Landroid/widget/RadioGroup;I)V',
                'android/widget/RadioGroup$OnCheckedChangeListener',
                'trackRadioGroup',
                '(Landroid/widget/RadioGroup;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        //在onStopTrackingTouch的方法最后插桩
        addInterfaceMethod(new MethodInfo(
                'onStopTrackingTouch',
                '(Landroid/widget/SeekBar;)V',
                'android/widget/SeekBar$OnSeekBarChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        //所有继承CoordinatorLayout$Behavior类的类的onStopNestedScroll方法都会被插桩，CoordinatorLayout$Behavior不是直接父类也会被插桩
        addSuperClassMethod(new MethodInfo(
                'onStopNestedScroll',
                '(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;I)V',
                'androidx/coordinatorlayout/widget/CoordinatorLayout$Behavior',
                'onBehaviorScrollStopStatic',
                '(Landroid/view/View;)V',
                3, 1,
                [Opcodes.ALOAD]))
        //在onScrollStateChanged的方法最后插桩
        addInterfaceMethod(new MethodInfo(
                'onScrollStateChanged',
                '(Landroid/widget/AbsListView;I)V',
                'android/widget/AbsListView$OnScrollListener',
                'onListScrollStateChangedStatic',
                '(Landroid/widget/AbsListView;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        //在onScroll的方法最后插桩
        addInterfaceMethod(new MethodInfo(
                'onScroll',
                '(Landroid/widget/AbsListView;III)V',
                'android/widget/AbsListView$OnScrollListener',
                'onListScrolledStatic',
                '(Landroid/widget/AbsListView;III)V',
                1, 4,
                [Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.ILOAD]))
        //在initialize的方法最后插桩
        addInterfaceMethod(new MethodInfo(
                'initialize',
                '(Landroidx/appcompat/view/menu/MenuItemImpl;I)V',
                'androidx/appcompat/view/menu/MenuView$ItemView',
                'onMenuItemInitializeStatic',
                '(Landroidx/appcompat/view/menu/MenuView$ItemView;Landroidx/appcompat/view/menu/MenuItemImpl;)V',
                0, 2,
                [Opcodes.ALOAD, Opcodes.ALOAD]))
        //所有实现了Adapter接口的类的getView方法都会被插桩，Adapter不是直接父类也会被插桩
        addSuperInterfaceMethod(new MethodInfo(
                'getView',
                '(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;',
                'android/widget/Adapter',
                'onListGetViewStatic',
                '(ILandroid/view/View;Landroid/view/ViewGroup;)V',
                1, 3,
                [Opcodes.ILOAD, Opcodes.ALOAD, Opcodes.ALOAD]))
        //所有继承RecyclerView$Adapter类的类的onBindViewHolder方法都会被插桩，RecyclerView$Adapter不是直接父类也会被插桩
        addSuperClassMethod(new MethodInfo(
                'onBindViewHolder',
                '(Landroidx/recyclerview/widget/RecyclerView$ViewHolder;I)V',
                'androidx/recyclerview/widget/RecyclerView$Adapter',
                'onRecyclerBindViewHolderStatic',
                '(Landroidx/recyclerview/widget/RecyclerView$ViewHolder;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        //所有继承RecyclerView$Adapter类的类的onBindViewHolder方法都会被插桩，RecyclerView$Adapter不是直接父类也会被插桩
        addSuperClassMethod(new MethodInfo(
                'onBindViewHolder',
                '(Landroidx/recyclerview/widget/RecyclerView$ViewHolder;ILjava.util.List;)V',
                'androidx/recyclerview/widget/RecyclerView$Adapter',
                'onRecyclerBindViewHolderStatic',
                '(Landroidx/recyclerview/widget/RecyclerView$ViewHolder;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        //给RecyclerView类本身的setAdapter方法进行插桩
        addClassMethod(new MethodInfo(
                'setAdapter',
                '(Landroidx/recyclerview/widget/RecyclerView$Adapter;)V',
                'androidx/recyclerview/widget/RecyclerView',
                'onSetRecyclerViewAdapterStatic',
                '(Landroidx/recyclerview/widget/RecyclerView;)V',
                0, 1,
                [Opcodes.ALOAD]))
        //所有继承RecyclerView$LayoutManager类的类的scrollToPosition方法都会被插桩，RecyclerView$LayoutManager不是直接父类也会被插桩
        addSuperClassMethod(new MethodInfo(
                'scrollToPosition',
                '(I)V',
                'androidx/recyclerview/widget/RecyclerView$LayoutManager',
                'onRecyclerViewScrollToPositionStatic',
                '(Landroidx/recyclerview/widget/RecyclerView$LayoutManager;)V',
                0, 1,
                [Opcodes.ALOAD]))
        //所有继承RecyclerView$LayoutManager类的类的scrollToPositionWithOffset方法都会被插桩，RecyclerView$LayoutManager不是直接父类也会被插桩
        addSuperClassMethod(new MethodInfo(
                'scrollToPositionWithOffset',
                '(II)V',
                'androidx/recyclerview/widget/RecyclerView$LayoutManager',
                'onRecyclerViewScrollToPositionWithOffsetStatic',
                '(Landroidx/recyclerview/widget/RecyclerView$LayoutManager;)V',
                0, 1,
                [Opcodes.ALOAD]))
        //给ViewPager类本身的setAdapter方法进行插桩
        addClassMethod(new MethodInfo(
                'setAdapter',
                '(Landroidx/viewpager/widget/PagerAdapter;)V',
                'androidx/viewpager/widget/ViewPager',
                'onSetViewPagerAdapterStatic',
                '(Landroidx/viewpager/widget/ViewPager;)V',
                0, 1,
                [Opcodes.ALOAD]))
    }

    static void addBeforeMethod(MethodInfo methodInfo) {
        if (methodInfo != null) {
            BEFORE_METHODS.put(methodInfo.parent + methodInfo.name + methodInfo.desc, methodInfo)
        }
    }

    static void addInterfaceMethod(MethodInfo methodInfo) {
        if (methodInfo != null) {
            INTERFACE_METHODS.put(methodInfo.parent + methodInfo.name + methodInfo.desc, methodInfo)
        }
    }
    static void addSuperInterfaceMethod(MethodInfo methodInfo) {
        if (methodInfo != null) {
            SUPER_INTERFACE_METHODS.put(methodInfo.name + methodInfo.desc, methodInfo)
        }
    }
    static void addClassMethod(MethodInfo methodInfo) {
        if (methodInfo != null) {
            CLASS_METHODS.put(methodInfo.parent + methodInfo.name + methodInfo.desc, methodInfo)
        }
    }
    static void addSuperClassMethod(MethodInfo methodInfo) {
        if (methodInfo != null) {
            SUPER_CLASS_METHODS.put(methodInfo.name + methodInfo.desc, methodInfo)
        }
    }
}
