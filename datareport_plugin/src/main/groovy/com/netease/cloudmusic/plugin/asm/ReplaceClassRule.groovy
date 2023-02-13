package com.netease.cloudmusic.plugin.asm

class ReplaceClassRule {

    //需要替换的父类的列表
    public final static HashMap<String, String> PARENT_CLASS = new HashMap<>()

    //构建匿名内部类的时候，需要替换的类
    public final static HashMap<String, String> INNER_CLASS = new HashMap<>()
    
    static {
        PARENT_CLASS.put("android/app/Dialog", "com/netease/cloudmusic/datareport/inject/dialog/ReportDialog")
        PARENT_CLASS.put("android/app/AlertDialog", "com/netease/cloudmusic/datareport/inject/dialog/ReportAlertDialog")
        PARENT_CLASS.put("android/app/ProgressDialog", "com/netease/cloudmusic/datareport/inject/dialog/ReportProgressDialog")

        PARENT_CLASS.put("android/app/Fragment", "com/netease/cloudmusic/datareport/inject/fragment/ReportFragment")
        PARENT_CLASS.put("android/app/ListFragment", "com/netease/cloudmusic/datareport/inject/fragment/ReportListFragment")
        PARENT_CLASS.put("android/app/DialogFragment", "com/netease/cloudmusic/datareport/inject/fragment/ReportDialogFragment")
        PARENT_CLASS.put("android/preference/PreferenceFragment", "com/netease/cloudmusic/datareport/inject/fragment/ReportPreferenceFragment")
        PARENT_CLASS.put("androidx/fragment/app/Fragment", "com/netease/cloudmusic/datareport/inject/fragment/ReportAndroidXFragment")
        PARENT_CLASS.put("android/widget/ScrollView", "com/netease/cloudmusic/datareport/inject/scroll/ReportScrollView")
        PARENT_CLASS.put("androidx/core/widget/NestedScrollView", "com/netease/cloudmusic/datareport/inject/scroll/ReportNestedScrollView")
        PARENT_CLASS.put("android/widget/HorizontalScrollView", "com/netease/cloudmusic/datareport/inject/scroll/ReportHorizontalScrollView")
        PARENT_CLASS.put("androidx/appcompat/app/AppCompatActivity", "com/netease/cloudmusic/datareport/inject/activity/ReportAppCompatActivity")
        PARENT_CLASS.put("android/app/Activity", "com/netease/cloudmusic/datareport/inject/activity/ReportActivity")
        PARENT_CLASS.put("androidx/fragment/app/FragmentActivity", "com/netease/cloudmusic/datareport/inject/activity/ReportFragmentActivity")

        INNER_CLASS.put("android/app/Dialog", "com/netease/cloudmusic/datareport/inject/dialog/ReportDialog")
        INNER_CLASS.put("android/app/ProgressDialog", "com/netease/cloudmusic/datareport/inject/dialog/ReportProgressDialog")
    }
    
}
