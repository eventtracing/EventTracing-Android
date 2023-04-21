package com.netease.cloudmusic.datareport.inject.fragment;

import android.app.Activity;
import android.view.View;

/**
 * 这是一个Fragment 参数兼容类，
 * 用于Fragment 消息通知过程中的参数传递。
 * <p>
 * 因为Fragment 可能存在三种包名，androidx和V4 不能同时打包，
 * 会导致触发系统的findclass 耗时
 * <p>
 * 目前只有view 和 activity，后续需要其他参数，可以添加
 */
public class FragmentCompat {

    private View mView;
    private Activity mActivity;

    /**
     * 设置Fragment view
     *
     * @param view
     */
    public void setView(View view) {
        mView = view;
    }

    /**
     * 设置Fragment Activity
     *
     * @param activity
     */
    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public View getView() {
        return mView;
    }

    public Activity getActivity() {
        return mActivity;
    }
}
