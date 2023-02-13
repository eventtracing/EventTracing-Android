package com.netease.cloudmusic.datareport.inject.fragment;

import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.inject.EventCollector;
import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.utils.UIUtils;

public class AndroidXFragmentCollector {

    private static final String TAG = "AndroidXFragmentCollect";

    public static void onResume(Fragment fragment) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onResume: fragment = " + fragment.getClass().getName());
        }
        if (fragment instanceof DialogFragment) {
            return;
        }
        EventCollector.getInstance().onFragmentResumed(fragmentToFragmentCompat(fragment));
    }

    public static void onPause(Fragment fragment) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onPause: fragment = " + fragment.getClass().getName());
        }
        if (fragment instanceof DialogFragment) {
            return;
        }
        EventCollector.getInstance().onFragmentPaused(fragmentToFragmentCompat(fragment));
    }

    public static void onHiddenChanged(Fragment fragment, boolean hidden) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden = " + hidden);
        }
        if (fragment instanceof DialogFragment) {
            return;
        }
        if (hidden) {
            EventCollector.getInstance().onFragmentPaused(fragmentToFragmentCompat(fragment));
        } else {
            EventCollector.getInstance().onFragmentResumed(fragmentToFragmentCompat(fragment));
        }
    }

    public static void setUserVisibleHint(Fragment fragment, boolean isVisibleToUser) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "setUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisible = " + isVisibleToUser);
        }
        if (fragment instanceof DialogFragment) {
            return;
        }
        if (isVisibleToUser) {
            EventCollector.getInstance().onFragmentResumed(fragmentToFragmentCompat(fragment));
        } else {
            EventCollector.getInstance().onFragmentPaused(fragmentToFragmentCompat(fragment));
        }
    }

    public static void onDestroyView(Fragment fragment) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onDestroyView: fragment = " + fragment.getClass().getName());
        }
        if (fragment instanceof DialogFragment) {
            return;
        }
        EventCollector.getInstance().onFragmentDestroyView(fragmentToFragmentCompat(fragment));
    }

    public static void onAndroidXFragmentViewCreated(Fragment fragment, View rootView) {
        if (!DataReportInner.getInstance().isDataCollectEnable()) {
            return;
        }
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.i(TAG, "onFragmentViewCreated: fragment = " + fragment.getClass().getName() + ", view = " + UIUtils.getViewInfo(rootView));
        }
    }

    public static FragmentCompat fragmentToFragmentCompat(Fragment fragment) {
        FragmentCompat compat = new FragmentCompat();
        compat.setActivity(fragment.getActivity());
        compat.setView(fragment.getView());
        return compat;
    }
}
