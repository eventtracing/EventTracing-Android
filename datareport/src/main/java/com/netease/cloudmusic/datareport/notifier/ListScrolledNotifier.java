package com.netease.cloudmusic.datareport.notifier;

import android.widget.AbsListView;

import com.netease.cloudmusic.datareport.data.ReusablePool;

/**
 * ListView滑动的事件通知者
 */
public class ListScrolledNotifier implements IEventNotifier {

    private AbsListView mListView;
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;

    public void init(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mListView = listView;
        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount = visibleItemCount;
        this.totalItemCount = totalItemCount;
    }

    @Override
    public int getReuseType() {
        return ReusablePool.TYPE_LIST_SCROLLED;
    }

    @Override
    public void notifyEvent(IEventListener listener) {
        listener.onListScrolled(mListView, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    @Override
    public void reset() {
        mListView = null;
    }
}
