package com.netease.cloudmusic.datareport.notifier;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.netease.cloudmusic.datareport.inject.fragment.FragmentCompat;

public interface IEventListener {

    void onActivityCreate(Activity activity);

    void onActivityStarted(Activity activity);

    void onActivityResume(Activity activity);

    void onActivityPause(Activity activity);

    void onActivityStopped(Activity activity);

    void onActivityDestroyed(Activity activity);

    /**
     * 弹框出现
     *
     * @param activity 被盖住的Activity
     * @param dialog   当前弹框
     */
    void onDialogShow(Activity activity, Dialog dialog);

    /**
     * 弹框消失
     *
     * @param activity 被盖住的Activity
     * @param dialog   当前弹框
     */
    void onDialogHide(Activity activity, Dialog dialog);

    void onFragmentResume(FragmentCompat fragment);

    void onFragmentPause(FragmentCompat fragment);

    void onFragmentDestroyView(FragmentCompat fragment);

    void onViewClick(View view);

    void onDialogClick(DialogInterface dialogInterface, int which);

    void onListScrollStateChanged(AbsListView listView, int scrollState);
    void onListScrolled(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount);

    void onSetRecyclerViewAdapter(RecyclerView recyclerView);

    void onSetViewPagerAdapter(ViewPager viewPager);

    void onViewReused(ViewGroup parentView, View view, long itemId);

    void onRecyclerViewScrollPosition(RecyclerView recyclerView);

    void onViewScroll(View scrollView);

}
