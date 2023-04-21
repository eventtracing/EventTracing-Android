package com.netease.cloudmusic.datareport.vtree.page;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;

import com.netease.cloudmusic.datareport.vtree.VTreeUtilsKt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

/**
 * dialog管理，所有activity对应的已经显示的dialog，用来做逻辑挂靠
 * 所有的activity都会挂靠在最右边的根节点上面
 */
public class DialogListUtil {

    private static WeakHashMap<Activity, List<WeakReference<Dialog>>> mActivityDialogMap = new WeakHashMap<>();

    public static void onActivityDestroy(Activity activity) {
        mActivityDialogMap.remove(activity);
    }

    public static void onDialogResume(Activity dialogActivity, Dialog dialog) {
        if (dialogActivity != null) {
            List<WeakReference<Dialog>> dialogList = getDialogList(dialogActivity);
            if (dialogList == null) {
                dialogList = new ArrayList<>();
                mActivityDialogMap.put(dialogActivity, dialogList);
            }
            saveDialog(dialog, dialogList);
        }
    }

    public static void onDialogStop(Activity dialogActivity, Dialog dialog) {
        if (dialogActivity != null) {
            List<WeakReference<Dialog>> dialogList = getDialogList(dialogActivity);
            if (dialogList != null) {
                removeDialog(dialog, dialogList);
            }
        }
    }

    public static List<WeakReference<Dialog>> getDialogList(Activity activity) {
        return mActivityDialogMap.get(activity);
    }

    public static Activity getDialogActivity(Dialog dialog) {
        Context context = dialog.getContext();
        Activity dialogActivity = null;
        do {
            if (context instanceof Activity) {
                dialogActivity = (Activity) context;
                break;
            }
            if (!(context instanceof ContextWrapper)) {
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        } while (true);
        dialogActivity = VTreeUtilsKt.changeTransparentActivity(dialogActivity);

        return dialogActivity;
    }

    private static void saveDialog(Dialog dialog, List<WeakReference<Dialog>> dialogList) {
        // 先删除已有的，去重
        removeDialog(dialog, dialogList);
        // 再加到末尾
        dialogList.add(new WeakReference<>(dialog));
    }

    private static void removeDialog(Dialog dialog, List<WeakReference<Dialog>> dialogList) {
        Iterator<WeakReference<Dialog>> iterator = dialogList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Dialog> dialogRef = iterator.next();
            if (dialogRef == null || dialogRef.get() == null || dialogRef.get() == dialog) {
                iterator.remove();
            }
        }
    }

}
