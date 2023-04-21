package com.netease.cloudmusic.datareport.notifier;

import androidx.viewpager.widget.ViewPager;

import com.netease.cloudmusic.datareport.data.ReusablePool;

/**
 * ViewPager设置adapter事件的事件通知者
 */
public class ViewPagerSetAdapterNotifier implements IEventNotifier {
    private ViewPager mView;

    public void init(ViewPager view) {
        mView = view;
    }

    @Override
    public int getReuseType() {
        return ReusablePool.TYPE_VIEW_PAGER_SET_ADAPTER;
    }

    @Override
    public void notifyEvent(IEventListener listener) {
        listener.onSetViewPagerAdapter(mView);
    }

    @Override
    public void reset() {
        mView = null;
    }
}
