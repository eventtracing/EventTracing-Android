package com.netease.cloudmusic.datareport.notifier;

import android.widget.AbsListView;

import com.netease.cloudmusic.datareport.data.ReusablePool;

/**
 * ListView滑动状态发生改变的事件通知者
 */
public class ListScrollNotifier implements IEventNotifier {

    private AbsListView mListView;
    private int mScrollState;

    public void init(AbsListView listView, int scrollState) {
        mListView = listView;
        mScrollState = scrollState;
    }

    @Override
    public int getReuseType() {
        return ReusablePool.TYPE_LIST_SCROLL;
    }

    @Override
    public void notifyEvent(IEventListener listener) {
        listener.onListScrollStateChanged(mListView, mScrollState);
    }

    @Override
    public void reset() {
        mListView = null;
    }
}
